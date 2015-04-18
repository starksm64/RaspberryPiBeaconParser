package org.jboss.summit2015.scanner.status.server;/*
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class LocalCommandHandler implements Runnable {
   private Socket client;
   private BeaconScannerServer server;
   private BufferedReader input;
   private BufferedWriter output;
   private boolean running;

   LocalCommandHandler(Socket client, BeaconScannerServer server) throws IOException {
      this.client = client;
      this.server = server;
      InputStream is = client.getInputStream();
      OutputStream os = client.getOutputStream();
      input = new BufferedReader(new InputStreamReader(is));
      output = new BufferedWriter(new OutputStreamWriter(os));
   }

   public void run() {
      running = true;
      try {
         doRun();
      } catch (Throwable e) {
         if (e instanceof EOFException)
            System.out.printf("IOException: client(%s) closed\n", client);
         else
            e.printStackTrace();
      } finally {
         try {
            input.close();
         } catch (Throwable e1) {
         }
         try {
            client.close();
         } catch (Throwable e1) {
         }
         System.err.printf("LocalCommandHandler exiting run loop\n");
      }
   }

   private void doRun() throws IOException {
      String cmd = input.readLine();
      while(running && cmd != null) {
         String reply = handleCommand(cmd.toUpperCase());
         if(reply == null)
            return;
         output.write(reply);
         output.write('\n');
         output.flush();
         cmd = input.readLine();
      }
   }
   private String handleCommand(String command) {
      String reply = "Unknown command";
      switch (command) {
         case "CONNS":
            int count = server.getHandlers().size();
            reply = String.format("Connections.count=%d", count);
            break;
         case "DBINFO":
            reply = server.getStore().getStatus();
            break;
         case "SHUTDOWN":
            reply = null;
            System.exit(0);
            break;
         case "QUIT":
            reply = null;
         default:
            break;
      }
      return reply;
   }
}
