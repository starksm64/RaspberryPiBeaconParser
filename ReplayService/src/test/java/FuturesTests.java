import com.google.gson.JsonObject;
import org.jboss.summit2015.replay.EventInTime;
import org.jboss.summit2015.replay.JsonEventStream;
import org.jboss.summit2015.replay.MsgPublisher;
import org.junit.Test;

import javax.jms.JMSException;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class FuturesTests {
   static int counter = 0;

   @Test
   public void testJsonObjectFields() throws Exception {
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject event = eventStream.next();
      System.out.printf("scannerID: %s\n", event.getAsJsonPrimitive("scannerID"));
      System.out.printf("uuid: %s\n", event.getAsJsonPrimitive("uuid"));
      System.out.printf("minor: %s\n", event.getAsJsonPrimitive("minor"));
      System.out.printf("minor.asInt: %d\n", event.getAsJsonPrimitive("minor").getAsInt());
      System.out.printf("time: %s\n", event.getAsJsonPrimitive("time"));
   }
   @Test
   public void testFutureEvent() throws Exception {
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject event = eventStream.next();
      long streamEpoch = event.getAsJsonPrimitive("time").getAsLong();
      long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);

      // Display the
      EventInTime e0 = new EventInTime(streamEpoch, streamEpoch, 0);
      event = eventStream.next();
      int minor = event.getAsJsonPrimitive("minor").getAsInt();
      long eventTime = event.getAsJsonPrimitive("time").getAsLong();
      EventInTime e1 = new EventInTime(streamEpoch, eventTime, minor);
      long replayTime = e1.getEventReplayTime(epoch);
      System.out.printf("Event1 original: %s, replay: %s, delta: %s\n", e1, new Date(replayTime), (replayTime - epoch));

      event = eventStream.next();
      eventTime = event.getAsJsonPrimitive("time").getAsLong();
      minor = event.getAsJsonPrimitive("minor").getAsInt();
      EventInTime e2 = new EventInTime(streamEpoch, eventTime, minor);
      replayTime = e2.getEventReplayTime(epoch);
      System.out.printf("Event2 original: %s, replay: %s, delta: %s\n", e2, new Date(replayTime), (replayTime - epoch));

      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      Date replayDate = new Date();

      for(int n = 3; n < 82823; n ++) {
         event = eventStream.next();
         eventTime = event.getAsJsonPrimitive("time").getAsLong();
         minor = event.getAsJsonPrimitive("minor").getAsInt();
         EventInTime et = new EventInTime(streamEpoch, eventTime, minor);
         replayTime = et.getEventReplayTime(epoch);
         long now = System.currentTimeMillis();
         long waitTime = replayTime - now;
         replayDate.setTime(replayTime);
         System.out.printf("Event%d original: %s, replay: %s, delta: %d, wait: %d\n", n, et, dtf.format(replayDate), (replayTime - epoch), waitTime);
      }
   }

   @Test
   public void testScheduledExecutorService() throws Exception {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject firstEvent = eventStream.next();
      long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
      final long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);
      Instant runEpochInstance = Instant.ofEpochMilli(epoch);

      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      System.out.printf("Begin scheduling the replay events...\n");
      while(eventStream.hasNext()) {
         JsonObject event = eventStream.next();
         int minor = event.getAsJsonPrimitive("minor").getAsInt();
         long eventTime = event.getAsJsonPrimitive("time").getAsLong();
         final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor);
         long delay = eventInTime.getReplayElapsedFromEpoch(epoch) + 1;
         scheduler.schedule(new Runnable() {
            @Override
            public void run() {
               counter++;
               if ((counter % 100) == 0) {
                  long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch);
                  Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                  Instant nowInstant = Instant.now();
                  Duration error = Duration.between(nowInstant, eventInstant);
                  Date now = new Date();
                  System.out.printf("%s running at: %s, error=%d\n", eventInTime, dtf.format(now), error.getSeconds());
               }
            }
         }, delay, TimeUnit.MILLISECONDS);
      }
      System.out.printf("Done scheduling the replay events\n");
      scheduler.awaitTermination(1, TimeUnit.DAYS);
   }

   @Test
   public void testScheduledExecutorServicex10() throws Exception {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject firstEvent = eventStream.next();
      long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
      final long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);
      Instant runEpochInstance = Instant.ofEpochMilli(epoch);

      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      System.out.printf("Begin scheduling the replay events...\n");
      while(eventStream.hasNext()) {
         JsonObject event = eventStream.next();
         int minor = event.getAsJsonPrimitive("minor").getAsInt();
         long eventTime = event.getAsJsonPrimitive("time").getAsLong();
         final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor);
         long delay = eventInTime.getReplayElapsedFromEpoch(epoch, 10);
         scheduler.schedule(new Runnable() {
            @Override
            public void run() {
               counter++;
               if ((counter % 1000) == 0) {
                  long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch, 10);
                  Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                  Instant nowInstant = Instant.now();
                  Duration error = Duration.between(nowInstant, eventInstant);
                  Date now = new Date();
                  System.out.printf("%s running at: %s, error=%d\n", eventInTime, dtf.format(now), error.getSeconds());
               }
            }
         }, delay, TimeUnit.MILLISECONDS);
      }
      System.out.printf("Done scheduling the replay events\n");
      scheduler.awaitTermination(1, TimeUnit.DAYS);
   }

   @Test
   public void testScheduledExecutorServicex50() throws Exception {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject firstEvent = eventStream.next();
      long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
      final long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);
      Instant runEpochInstance = Instant.ofEpochMilli(epoch);

      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      System.out.printf("Begin scheduling the replay events...\n");
      float speedup = 50;
      while(eventStream.hasNext()) {
         JsonObject event = eventStream.next();
         int minor = event.getAsJsonPrimitive("minor").getAsInt();
         long eventTime = event.getAsJsonPrimitive("time").getAsLong();
         final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor);
         long delay = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
         scheduler.schedule(new Runnable() {
            @Override
            public void run() {
               counter++;
               if ((counter % 1000) == 0) {
                  long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
                  Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                  Instant nowInstant = Instant.now();
                  Duration error = Duration.between(nowInstant, eventInstant);
                  Date now = new Date();
                  System.out.printf("%s running at: %s, error=%d\n", eventInTime, dtf.format(now), error.getSeconds());
               }
            }
         }, delay, TimeUnit.MILLISECONDS);
      }
      System.out.printf("Done scheduling the replay events\n");
      scheduler.awaitTermination(1, TimeUnit.DAYS);
   }

   @Test
   public void testScheduledExecutorServicex50Cancel5000() throws Exception {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject firstEvent = eventStream.next();
      long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
      final long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);
      Instant runEpochInstance = Instant.ofEpochMilli(epoch);

      final CountDownLatch limitCount = new CountDownLatch(5000);
      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      System.out.printf("Begin scheduling the replay events...\n");
      float speedup = 50;
      while(eventStream.hasNext()) {
         JsonObject event = eventStream.next();
         int minor = event.getAsJsonPrimitive("minor").getAsInt();
         long eventTime = event.getAsJsonPrimitive("time").getAsLong();
         final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor);
         long delay = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
         final Future future = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
               counter++;
               limitCount.countDown();
               if ((counter % 1000) == 0) {
                  long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
                  Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                  Instant nowInstant = Instant.now();
                  Duration error = Duration.between(nowInstant, eventInstant);
                  Date now = new Date();
                  System.out.printf("%s running at: %s, error=%d\n", eventInTime, dtf.format(now), error.getSeconds());
               }
            }
         }, delay, TimeUnit.MILLISECONDS);
      }
      System.out.printf("Done scheduling the replay events\n");
      // Wait for
      limitCount.await();
      List<Runnable> remaining = scheduler.shutdownNow();
      System.out.printf("Shutdown, remaining count=%d\n", remaining.size());
   }

   @Test
   public void testScheduledExecutorServicex50PublishCancel5000() throws Exception {
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      JsonEventStream eventStream = new JsonEventStream();
      eventStream.init("SixScannersRun-2015-03-31.json.gz");
      // Get the first event to synchronize the time
      JsonObject firstEvent = eventStream.next();
      long streamEpoch = firstEvent.getAsJsonPrimitive("time").getAsLong();
      final long epoch = System.currentTimeMillis();
      long streamOffset = epoch - streamEpoch;
      System.out.printf("Stream epoch: %s(%d), now=%s(%d), offset=%d\n", new Date(streamEpoch), streamEpoch,
         new Date(epoch), epoch, streamOffset);
      Instant runEpochInstance = Instant.ofEpochMilli(epoch);

      final CountDownLatch limitCount = new CountDownLatch(50000);
      final DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);
      System.out.printf("Begin scheduling the replay events...\n");
      MsgPublisher publisher = new MsgPublisher();
      publisher.init();
      float speedup = 50;
      while(eventStream.hasNext()) {
         JsonObject event = eventStream.next();
         int minor = event.getAsJsonPrimitive("minor").getAsInt();
         long eventTime = event.getAsJsonPrimitive("time").getAsLong();
         final EventInTime eventInTime = new EventInTime(streamEpoch, eventTime, minor, event);
         long delay = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
         final Future future = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
               counter++;
               limitCount.countDown();
               if ((counter % 1000) == 0) {
                  long elapsedFromEpoch = eventInTime.getReplayElapsedFromEpoch(epoch, speedup);
                  Instant eventInstant = runEpochInstance.plusMillis(elapsedFromEpoch);
                  Instant nowInstant = Instant.now();
                  Duration error = Duration.between(nowInstant, eventInstant);
                  Date now = new Date();
                  System.out.printf("%s running at: %s, error=%d\n", eventInTime, dtf.format(now), error.getSeconds());
               }
               try {
                  publisher.publishEvent(eventInTime.getJson());
               } catch (JMSException e) {
                  e.printStackTrace();
               }
            }
         }, delay, TimeUnit.MILLISECONDS);
      }
      System.out.printf("Done scheduling the replay events\n");
      // Wait for
      limitCount.await();
      List<Runnable> remaining = scheduler.shutdownNow();
      System.out.printf("Shutdown, remaining count=%d\n", remaining.size());
   }

   @Test
   public void testTimeDiffs() {
      // Stream epoch: 14:49:34 PDT 2015(1427838574262), now= 19:41:45 PDT 2015(1427856105882), offset=17531620
      // 20@2:49:55 PM PDT running at: 7:41:48 PM PDT, error=19418
      // 60@3:18:09 PM PDT running at: 7:44:37 PM PDT, error=1543231
      Instant streamStart = Instant.ofEpochMilli(1427838574262L);
      Instant runStart = Instant.ofEpochMilli(1427856105882L);
      System.out.printf("streamStart = %s\n", streamStart);
      System.out.printf("runStart = %s\n", runStart);
      LocalDateTime streamStartLDT = LocalDateTime.ofInstant(streamStart, ZoneId.systemDefault());
      LocalDateTime runStartLDT = LocalDateTime.ofInstant(runStart, ZoneId.systemDefault());
      System.out.printf("streamStartLDT = %s\n", streamStartLDT);
      System.out.printf("runStartLDT = %s\n", runStartLDT);

      Instant endStream = streamStart.plus(29, ChronoUnit.MINUTES).plusSeconds(35);
      LocalDateTime endStreamLDT = LocalDateTime.ofInstant(endStream, ZoneId.systemDefault());
      System.out.printf("endStreamLDT = %s\n", endStreamLDT);
      Instant endRun = runStart.plus(29, ChronoUnit.MINUTES).plusSeconds(35);
      LocalDateTime endRunLDT = LocalDateTime.ofInstant(endRun, ZoneId.systemDefault());
      System.out.printf("endRunLDT = %s\n", endRunLDT);
      long seconds = 29*60+35;
      Instant endRunx10 = runStart.plusSeconds(seconds/10);
      LocalDateTime endRunx10LDT = LocalDateTime.ofInstant(endRunx10, ZoneId.systemDefault());
      System.out.printf("endRunx10LDT = %s\n", endRunx10LDT);

      // 20@2:49:55 PM PDT running at: 10:26:58 PM PDT, error=-1
      //
   }
}
