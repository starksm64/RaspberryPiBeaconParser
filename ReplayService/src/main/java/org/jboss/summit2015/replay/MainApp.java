package org.jboss.summit2015.replay;

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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2015 Red Hat Inc.
 */
public class MainApp implements ReplayCompleteCallback {
   private static final Logger log = Logger.getLogger(ReplayService.class);
   private static ConcurrentHashMap<String, ReplayManager> managerTasks = new ConcurrentHashMap<>();

   ExecutorService managedExecutorService = Executors.newSingleThreadExecutor();

   @Override
   public void complete(ReplayManager manager) {
      System.out.printf("complete: %s\n", manager);
      String id = manager.getID();
      managerTasks.remove(id);
   }

   void run() {
      String dataSetName = "SevenScannersRun-2015-05-11.json.gz";
      float speedup = 0.2f;
      int limitCount = -1;

      ReplayManager replayManager = new ReplayManager();
      log.infof("Created raw ReplayManager\n");
      replayManager.setDataSet(dataSetName);
      replayManager.setSpeedup(speedup);
      replayManager.setCompleteCallback(this);
      if (limitCount > 0)
         replayManager.setLimitCount(limitCount);
      replayManager.connect();

      Future future = managedExecutorService.submit(replayManager);
      replayManager.setFuture(future);
      log.infof("Submitted replay manager task: %s", replayManager);
      managerTasks.put(replayManager.getID(), replayManager);
   }

   public static void main(String[] args) throws InterruptedException {
      MainApp app = new MainApp();
      int runCount = 0;
      while(runCount < 1000) {
         app.run();
         System.out.printf("%d, Waiting for completion...\n", runCount);
         while (managerTasks.size() > 0) {
            Thread.sleep(5000);
         }
         System.out.printf("End main\n");
         runCount ++;
      }
   }
}
