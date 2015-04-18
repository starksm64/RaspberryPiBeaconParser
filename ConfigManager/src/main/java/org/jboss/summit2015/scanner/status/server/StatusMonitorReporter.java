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

import org.jboss.summit2015.scanner.status.StatusProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class StatusMonitorReporter implements Runnable, StatusMonitor {
   static volatile ConcurrentHashMap<String, ScannerInfo> scannerHeartbeats = new ConcurrentHashMap<>();
   boolean running;

   static class ScannerInfo {
      Map<String,String> lastStatus;
      long time;
      int publishCount;

      public ScannerInfo(long time, int publishCount, Map<String,String> lastStatus) {
         this.time = time;
         this.publishCount = publishCount;
         this.lastStatus = lastStatus;
      }
      public String toString(long now) {
         return String.format("%d,%d", (now-time)/1000, publishCount);
      }
   }

   @Override
   public void run() {
      running = true;
      while(running) {
         long now = System.currentTimeMillis();
         ArrayList<String> summary = new ArrayList<>();
         scannerHeartbeats.forEach((key, value) -> {
            long diff = now - value.time;
            if (diff > 60000)
               System.err.printf("No heartbeat from %s for %d seconds\n", key, diff / 1000);
            summary.add(String.format("%s: %s", key, value.toString(now)));
         });
         System.out.printf("{%d}:%s; %s\n", summary.size(), new Date(now), summary);
         try {
            Thread.sleep(15000);
         } catch (InterruptedException e) {
         }
      }
   }

   @Override
   public void monitor(Map<String, String> scannerStatus) {
      String scannerID = scannerStatus.get(StatusProperties.ScannerID.name());
      String timeStr = scannerStatus.get(StatusProperties.SystemTimeMS.name());
      String publishCountStr = scannerStatus.get(StatusProperties.PublishEventCount.name());
      long time;
      try {
         time = Long.parseLong(timeStr);
      } catch (NumberFormatException e) {
         System.err.printf("Failed to parse time: %s\n", timeStr);
         System.err.printf("scannerStatus: %s\n", scannerStatus);
         return;
      }
      int publishCount = Integer.parseInt(publishCountStr);
      ScannerInfo info = new ScannerInfo(time, publishCount, scannerStatus);
      scannerHeartbeats.put(scannerID, info);
   }
}
