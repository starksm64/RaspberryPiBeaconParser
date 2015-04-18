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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.InvalidPropertiesFormatException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ScannerHandler implements Runnable {
   private MessageStore store;
   private HandlerTermination doneCallback;
   private Socket client;
   private SocketAddress clientAddress;
   private DataInputStream clientStream;
   private boolean running;

   public ScannerHandler(Socket client, MessageStore store, HandlerTermination doneCallback) throws IOException {
      this.client = client;
      this.clientAddress = client.getRemoteSocketAddress();
      this.store = store;
      this.doneCallback = doneCallback;
      InputStream is = client.getInputStream();
      clientStream = new DataInputStream(is);
   }

   public String toString() {
      return clientAddress.toString();
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
         try {
            clientStream.close();
         } catch (IOException e1) {
         }
         try {
            client.close();
         } catch (IOException e1) {
         }
      } finally {
         System.err.printf("Exiting run loop\n");
         doneCallback.terminated(this);
      }
   }

   /**
    * Message format is length, byte[length] = destinationID, length, byte[length] = body
    * @throws IOException
    */
   private void doRun() throws IOException {
      while(running) {
         int length = clientStream.readInt();
//         System.out.printf("Reading destination of size: %d\n", length);
         byte[] destinationID = new byte[length];
         clientStream.readFully(destinationID);
//         System.out.printf("destinationID=%s\n", new String(destinationID));
         length = clientStream.readInt();
//         System.out.printf("Reading msg of size: %d\n", length);
         byte[] msg = new byte[length];
         clientStream.readFully(msg);
         try {
            store.store(destinationID, msg);
         } catch (InvalidPropertiesFormatException e) {
            // Exception thrown to log the client connection generating the error
            System.err.printf("Invalid properties msg sent by: %s\n", this);
         }
      }
   }

}
