package org.jboss.summit2015.beacon;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class StreamServer extends AbstractParser {
   private static Logger log = Logger.getLogger(StreamServer.class);
   private StreamCommand streamCmd;
   private ExecutorService writeService;
   private LinkedBlockingDeque<String> lines;
   private volatile Socket activeClient;
   private volatile boolean running;

   public StreamServer(StreamCommand streamCmd) throws IOException {
      this.streamCmd = streamCmd;
      this.lines = new LinkedBlockingDeque<>(streamCmd.linesBacklog);
   }

   public void processHCIStream(final InputStream is) {
      running = true;
      writeService = Executors.newFixedThreadPool(2);
      // Start a thread to read the HCI dump and place into the lines queue
      writeService.submit(() -> readHCI(is));
      // Accept a client
      log.info("Entering client accept loop");
      try (ServerSocket serverSocket = new ServerSocket(streamCmd.port, streamCmd.backlog, streamCmd.bindAddr)) {
         while(running) {
            // Schedule a write task
            log.infof("Waiting for socket client at: %s", serverSocket.getLocalSocketAddress());
            Socket client = serverSocket.accept();
            if(activeClient != null) {
               // Send client an error indicating only one connection at a time is valid
               log.infof("Rejecting duplicate socket client from: %s", client.getRemoteSocketAddress());
               client.getOutputStream().write("Only one client connection at a time is allowed".getBytes());
               client.close();
            }
            activeClient = client;
            writeService.submit(() -> writeHCI());
            // Only accept one client if we are using test data
            if(isUsingTestData())
               break;
         }
      } catch (IOException e) {
         log.error("Failed to accept client", e);
      }
      log.info("Exiting client accept loop");
   }

   public void cleanup() {
      if(writeService != null) {
         log.info("Shutting down write service...");
         writeService.shutdown();
         writeService = null;
         log.info("done");
      }
   }

   public boolean isRunning() {
      return running;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   private void readHCI(InputStream is) {
      log.info("Begin reading HCI raw output");
      try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
         String line;
         while ((line = br.readLine()) != null) {
            if(lines.offerLast(line) == false) {
               lines.removeFirst();
               lines.addLast(line);
            }
         }
      } catch (IOException e) {
         log.error("Failure while reading HCI stream", e);
      }
      log.info("End reading HCI raw output");
   }

   private void writeHCI() {
      log.infof("Sending HCI stream to: %s", activeClient.getRemoteSocketAddress());
      try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(activeClient.getOutputStream()))) {
         String line = lines.peekFirst();
         while(running || line != null) {
            if (line != null) {
               lines.removeFirst();
               bw.write(line);
               bw.newLine();
            } else {
               // Flush the buffer
               bw.flush();
               // If the data was test data, break to close the client
               if(isUsingTestData())
                  break;
            }
            line = lines.peekFirst();
         }
      } catch (IOException e) {
         log.error("Failure while writing HCI stream", e);
      }
      log.infof("End HCI stream to: %s", activeClient.getRemoteSocketAddress());
      try {
         activeClient.close();
      } catch (IOException e) {
      }
      //
      activeClient = null;
   }
}
