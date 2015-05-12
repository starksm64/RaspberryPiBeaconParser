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

import com.google.gson.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ReplayManager implements Runnable {
   private static Logger log = Logger.getLogger(ReplayManager.class);

   private FileWriter debug;
   private String dataSet;
   private float speedup;
   private String queueName;
   private int limitCount = -1;
   private int count;
   private int taskCount;
   private int onePctCount;
   private ScheduledExecutorService scheduler;
   private ReplayCompleteCallback completeCallback;
   private OutputStream outputStream;
   private boolean done;

   //@Inject
   private MsgPublisher publisher;
   private Future future;

   @PostConstruct
   public void init() {
      log.info("init");
   }
   @PreDestroy
   public void destroy() {
      log.info("destroy", new Exception("where?"));
   }
   public String getDataSet() {
      return dataSet;
   }

   public String getID() {
      return publisher.getClientID();
   }
   public void setDataSet(String dataSet) {
      this.dataSet = dataSet;
   }

   public float getSpeedup() {
      return speedup;
   }

   public void setSpeedup(float speedup) {
      this.speedup = speedup;
   }

   public String getQueueName() {
      return queueName;
   }

   public void setQueueName(String queueName) {
      this.queueName = queueName;
      publisher.setDestinationName(queueName);
   }

   public int getLimitCount() {
      return limitCount;
   }

   public void setLimitCount(int limitCount) {
      this.limitCount = limitCount;
   }

   public ReplayCompleteCallback getCompleteCallback() {
      return completeCallback;
   }

   public void setCompleteCallback(ReplayCompleteCallback completeCallback) {
      this.completeCallback = completeCallback;
   }

   public void connect() {
      publisher = new MsgPublisher();
      publisher.init();
      log.infof("Created MsgPublisher\n");
   }

   public void run() {
      final CountDownLatch limitCountLatch = limitCount > 0 ? new CountDownLatch(limitCount) : null;
      scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
         @Override
         public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "ReplayManagerThread");
            t.setPriority(Thread.MAX_PRIORITY);
            t.setDaemon(true);
            return t;
         }
      });
      done = false;
      JsonEventStream eventStream = new JsonEventStream();
      try {
         debug = new FileWriter("/tmp/ReplayManager.log");
         eventStream.init(dataSet);
         // Get the first event to synchronize the time
         JsonObject firstEvent = eventStream.next();
         long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
         final long epoch = System.currentTimeMillis();
         Instant runEpochInstance = Instant.ofEpochMilli(epoch);
         long streamOffset = epoch - streamEpoch;
         log.infof("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
            new Date(epoch), epoch, streamOffset);
         taskCount = 0;
         log.infof("Begin scheduling the replay events...\n");
         while (eventStream.hasNext()) {
            JsonObject event = eventStream.next();
            int minor = event.getAsJsonPrimitive("minor").getAsInt();
            long eventTime = event.getAsJsonPrimitive("time").getAsLong();
            final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor, event);
            long delay = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
            Object[] params = {minor, eventTime, delay, new Date(epoch+delay)};
            log.debugf("Schedule(%d,%d) delayMS=%d[%s]", params);
            debug.write(String.format("Schedule(%d,%d) delayMS=%d[%s]\n", minor, eventTime, delay, new Date(epoch + delay)));
            scheduler.schedule(new Runnable() {
               @Override
               public void run() {
                  if (limitCountLatch != null) {
                     if (limitCountLatch.getCount() == 0) {
                        log.debugf("Skipping event(%d): %s\n", count, eventInTime.getJson());
                        return;
                     }
                     limitCountLatch.countDown();
                  }

                  JsonObject json = eventInTime.getJson();
                  try {
                     publisher.publishEvent(json, System.currentTimeMillis());
                     count++;
                     if (count % 500 == 0)
                        publisher.commit();
                     //publisher.publishTestMessage();
                  } catch (JMSException e) {
                     log.warn("Failed to publish: " + event, e);
                  }
                  if (count % onePctCount == 0) {
                     int pct = count / onePctCount;
                     long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
                     Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                     Instant nowInstant = Instant.now();
                     Duration error = Duration.between(nowInstant, eventInstant);
                     String msg = String.format("Replay status, pct=%d%%, errorSecs=%d", pct, error.getSeconds());
                     if (outputStream != null) {
                        try {
                           outputStream.write(msg.getBytes());
                           outputStream.write('\n');
                           outputStream.flush();
                        } catch (IOException e) {
                           log.error("Failed to write status", e);
                        }
                     }
                     log.infof(msg);
                     System.out.printf("%s\n", msg);
                  }
               }
            }, delay, TimeUnit.MILLISECONDS);
            taskCount ++;
            if(limitCount > 0 && taskCount > limitCount) {
               log.infof("Exiting on limitCount=%d, taskCount=%d", limitCount, taskCount);
               break;
            }
         }
         debug.close();
         eventStream.close();
         int endCount = limitCount > 0 ? limitCount : taskCount;
         onePctCount = endCount / 100;
         if(onePctCount == 0)
            onePctCount = 1;
         System.out.printf("Done scheduling the replay events, taskCount=%d, limitCount=%d, onePctCount=%d\n",
                     taskCount, limitCount, onePctCount);
         log.infof("Done scheduling the replay events, taskCount=%d, limitCount=%d, onePctCount=%d\n",
            taskCount, limitCount, onePctCount);
         if(!scheduler.isShutdown()) {
            if (limitCountLatch != null) {
               log.infof("Waiting on limit count down latch=%d/%d\n", limitCountLatch.getCount(), limitCount);
               limitCountLatch.await();
               log.infof("Countdown complete, shutting down replay executor\n");
               List<Runnable> remaining = scheduler.shutdownNow();
               log.infof("Shutdown, remaining count=%d\n", remaining.size());
            } else {
               while(count < taskCount)
                  Thread.sleep(5000);
               log.info("Shutting down replay executor...");
               scheduler.shutdown();
               log.infof("shutdown replay executor\n");
               scheduler.awaitTermination(1, TimeUnit.DAYS);
               log.infof("replay executor termination complete\n");
            }
            if(completeCallback != null) {
               publisher.commit();
               publisher.close();
               completeCallback.complete(this);
            }
         }
         // Notify any waiting StreamingOutput instance
         synchronized (this) {
            done = true;
            notifyAll();
            log.info("Notified all waiting StreamingOutputs");
         }
      } catch (Throwable e) {
         log.error("Failure during the replay", e);
      }
      log.infof("Exit run, this=%s", toString());
   }

   public synchronized void cancel() {
      if(scheduler != null) {
         log.infof("Cancelling, %s", publisher.getClientID());
         List<Runnable> remaining = scheduler.shutdownNow();
         log.infof("Cancelled, remaining count=%d\n", remaining.size());
         publisher.close();
      }
   }

   public void setFuture(Future future) {
      this.future = future;
   }

   public Future getFuture() {
      return future;
   }

   @Override
   public String toString() {
      float pctComplete = 100 * count;
      int endCount = limitCount > 0 ? limitCount : taskCount;
      pctComplete /= endCount;
      return String.format("ReplayManager(id=%s){dataSet=%s, speedup=%.1f, queueName=%s, limitCount=%d, pctComplete=%.2f%%(%d/%d)}",
         publisher.getClientID(), dataSet, speedup, queueName, limitCount, pctComplete, count, endCount);
   }

   public StreamingOutput getStreamingOutput() {
      return new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws IOException, WebApplicationException {
            ReplayManager.this.outputStream = output;
            log.info("Begin StreamingOutput");
            String header = String.format("<html><body><h1>Begin, %s</h1><pre>", new Date());
            output.write(header.getBytes());

            synchronized (this) {
               while(!done) {
                  try {
                     this.wait(5000);
                  } catch (InterruptedException e) {
                     log.warn("Interrupted while waiting for completion", e);
                  }
               }
            }
            String footer = String.format("</pre>Done, %s</pre></body></html>", new Date());
            output.write(footer.getBytes());
            output.flush();
            log.info("End StreamingOutput");
         }
      };
   }
}
