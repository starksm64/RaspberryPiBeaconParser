package org.jboss.summit2015.scanner.status.server;
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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple socket server that collects beacon scanner data
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconScannerServer implements HandlerTermination {
   public static final String DB_ROOT = "/Users/starksm/Dev/IoT/BLE/BeaconScannerServer.data";
   private ServerSocket serverSocket;
   private ExecutorService handlersExecutor;
   private MessageStore store;
   private StatusMonitorReporter statusReporter = new StatusMonitorReporter();
   private HashSet<ScannerHandler> handlers = new HashSet<>();
   private boolean useLocalCommandHandler = true;

   BeaconScannerServer() throws IOException {
      serverSocket = new ServerSocket(12345, 4, InetAddress.getLocalHost());
      handlersExecutor = Executors.newWorkStealingPool(8);
      ScannerEventsDB eventsDB = new ScannerEventsDB();
      File dbRoot = new File(DB_ROOT);
      dbRoot.mkdir();
      eventsDB.setup(dbRoot, false);
      eventsDB.showStatus();
      eventsDB.setStatusReporter(statusReporter);
      store = eventsDB;
   }

   public ServerSocket getServerSocket() {
      return serverSocket;
   }

   public ExecutorService getHandlersExecutor() {
      return handlersExecutor;
   }

   public MessageStore getStore() {
      return store;
   }

   public HashSet<ScannerHandler> getHandlers() {
      return handlers;
   }

   @Override
   public void terminated(ScannerHandler handler) {
      boolean removed = handlers.remove(handler);
      if(removed) {
         System.err.printf("Removed scanner: %s, remaining=%d\n", handler, handlers.size());
      }
   }

   public void run() throws IOException {
      handlersExecutor.submit(statusReporter);
      InetAddress thisAddress = serverSocket.getInetAddress();
      System.out.printf("Waiting for clients at: %s\n", serverSocket.toString());
      Socket client = serverSocket.accept();
      while(client != null) {
         System.out.printf("Accepted client: %s\n", client);
         InetAddress remoteAddress = client.getInetAddress();
         if(useLocalCommandHandler && (remoteAddress.isLoopbackAddress() || remoteAddress.equals(thisAddress)) ) {
            LocalCommandHandler cmdHandler = new LocalCommandHandler(client, this);
            handlersExecutor.submit(cmdHandler);
         } else {
            ScannerHandler handler = new ScannerHandler(client, store, this);
            handlersExecutor.submit(handler);
            handlers.add(handler);
         }
         client = serverSocket.accept();
      }
   }

   void shutdown() {
      System.out.printf("Shutting down server...");
      try {
         serverSocket.close();
         System.out.printf("socket...");
      } catch (Throwable e) {
      }
      System.out.printf("store...");
      store.close();
      handlersExecutor.shutdownNow();
      System.out.printf("executor, done\n");
      System.out.flush();
   }

   public static void main(String[] args) throws IOException {
      final BeaconScannerServer server = new BeaconScannerServer();
      Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run() {
            server.shutdown();
         }
      });
      try {
         server.run();
      } catch (Throwable e) {
      }
   }
}
