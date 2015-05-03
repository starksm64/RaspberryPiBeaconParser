import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
import org.jboss.summit2015.beacon.Beacon;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class EntryExitEvents {
   static DateFormat df = new SimpleDateFormat("H:m:s");
   static long DURATION = 15000;

   static class FileSupplier implements Supplier<Beacon> {
      long firstBucketStart;
      int bucketCount;
      private long bucketStart;
      private long bucketEnd;
      boolean bucketComplete = false;
      HashMap<String, AvgBeacon> eventByBeacon = new HashMap<>();
      Iterator<AvgBeacon> bucketIterator;
      JsonStreamParser jsp;
      Gson gson;

      FileSupplier() throws IOException {
         String dataSet = "FourScannersRun1-2015-03-10.json.gz";
         gson = new Gson();
         jsp = getDataSetParser(dataSet);
      }
      @Override
      public Beacon get() {
         if(jsp.hasNext() == false)
            return null;

         if(bucketComplete) {
            if(bucketIterator == null)
               bucketIterator = eventByBeacon.values().iterator();
            if(bucketIterator.hasNext()) {
               AvgBeacon avg = bucketIterator.next();
               Beacon event = avg.getAvgBeacon();
               return event;
            }
            bucketComplete = false;
            bucketIterator = null;
            eventByBeacon.clear();
            // Next bucket
            Date d0 = new Date(bucketStart);
            Date d1 = new Date(bucketEnd);
            System.out.printf("Processing(%d): %s-%s\n", bucketCount, df.format(d0), df.format(d1));
         }

         // Fill the next bucket
         do {
            if(jsp.hasNext() == false) {
               System.out.printf("How does this happen???\n");
               return null;
            }
            JsonElement jse = jsp.next();
            Beacon beacon = gson.fromJson(jse, Beacon.class);
            int tag = beacon.getMinor();
            String key = beacon.getScannerID() + ':' + tag;
            long time = beacon.getTime();
            if(beacon.getMessageType() != 0)
               continue;

            if (bucketStart == 0) {
               bucketStart = time;
               if(firstBucketStart == 0)
                  firstBucketStart = time;
               bucketEnd = bucketStart + DURATION;
               Date d0 = new Date(bucketStart);
               Date d1 = new Date(bucketEnd);
               System.out.printf("Processing(%d): %s-%s\n", bucketCount, df.format(d0), df.format(d1));
            }
            // Get accumluation beacon event
            AvgBeacon bucket = eventByBeacon.get(key);
            long elapsed = time - bucketStart;
            if (bucket == null) {
               bucket = new AvgBeacon(beacon);
               eventByBeacon.put(key, bucket);
            }
            else if (elapsed <= DURATION) {
               // Avg
               bucket.update(beacon);
            } else {
               bucketComplete = true;
               bucketStart = bucketEnd;
               bucketEnd += DURATION;
               bucketCount ++;
            }
         } while(bucketComplete == false);

         return get();
      }
   }


   static class TimeBucketCombiner implements BinaryOperator<Beacon> {
      private long bucketStart;
      private long bucketEnd;

      @Override
      public Beacon apply(Beacon beacon, Beacon beacon2) {
         long time1 = beacon.getTime();
         return beacon;
      }
   }

   static class TestSpliterator implements Spliterator<Beacon> {
      private ConcurrentLinkedDeque<JsonElement> beacons = new ConcurrentLinkedDeque<>();
      JsonStreamParser jsp;
      int[] ids;
      Gson gson;

      class BeaconSpliterator implements Spliterator<Beacon> {
         int id;
         int splitCount;
         JsonStreamParser jsp;

         BeaconSpliterator(int id, JsonStreamParser jsp) {
            this.id = id;
         }

         @Override
         public boolean tryAdvance(Consumer<? super Beacon> action) {
            JsonElement jse = beacons.peekFirst();
            Beacon beacon = gson.fromJson(jse, Beacon.class);
            action.accept(beacon);
            return true;
         }

         @Override
         public Spliterator<Beacon> trySplit() {
            if(splitCount < ids.length)
               return new BeaconSpliterator(ids[splitCount++], jsp);
            return null;
         }

         @Override
         public long estimateSize() {
            return 0;
         }

         @Override
         public int characteristics() {
            return 0;
         }
      }
      TestSpliterator(int[] ids) {
         this.ids = ids;
      }
      @Override
      public boolean tryAdvance(Consumer<? super Beacon> action) {

         JsonElement jse = null;
         synchronized (jsp) {
            if(jsp.hasNext() == false)
               return false;
            jse = jsp.next();
         }
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         action.accept(beacon);
         return true;
      }

      @Override
      public Spliterator<Beacon> trySplit() {
         return null;
      }

      @Override
      public long estimateSize() {
         return 0;
      }

      @Override
      public int characteristics() {
         return 0;
      }

      TestSpliterator() throws IOException {
         String dataSet = "FourScannersRun1-2015-03-10.json.gz";
         // Get the dataSet input stream directly from git
         URL gitURL = new URL("file:/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/data/" + dataSet);
         InputStream is = gitURL.openStream();
         GZIPInputStream gzip = new GZIPInputStream(is);
         InputStreamReader reader = new InputStreamReader(gzip);
         gson = new Gson();
         jsp = new JsonStreamParser(reader);
      }

   }

   static JsonStreamParser getDataSetParser(String dataSet) throws IOException {
      // Get the dataSet input stream directly from git
      URL gitURL = new URL("file:/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/data/" + dataSet);
      InputStream is = gitURL.openStream();
      GZIPInputStream gzip = new GZIPInputStream(is);
      InputStreamReader reader = new InputStreamReader(gzip);
      JsonStreamParser jsp = new JsonStreamParser(reader);
      return jsp;
   }
   /**
    * Simple test of processing the json data using a serial spliterator
    * @throws IOException
    */
   static void basicStreamTest() throws IOException {
      Stream<Beacon> eventStream = StreamSupport.stream(new TestSpliterator(), false);
      //Supplier<Beacon> supplier = new TestSupplier();
      Comparator<Beacon> cmp = (o1, o2) -> {
         int compare = o1.getMinor() - o2.getMinor();
         if(compare == 0) {
            compare = (int) (o1.getTime() - o2.getTime());
         }
         return compare;
      };
      eventStream.filter(b -> b.getMinor() < 20).sorted(cmp).forEach(e -> System.out.printf("%s\n", e));
   }
   /**
    * Test of processing the json data using a serial spliterator with collection into an N second time window
    * @throws IOException
    */
   static void basicStreamCollectTest() throws IOException {
   }

   /**
    * Non stream version of N second window creation
    * @param jsp
    * @return
    */
   static List<Beacon> averageEvents(JsonStreamParser jsp) {
      Gson gson = new Gson();
      ArrayList<Beacon> windows = new ArrayList<>();
      HashMap<Integer, HashMap<String, AvgBeacon>> eventWindowByBeacon = new HashMap<>();
      long bucketStart = 0;
      long bucketEnd = 0;
      long firstTime = 0;
      int bucketCount = 0;

      while(jsp.hasNext()) {
         JsonElement jse = jsp.next();
         JsonPrimitive messageType = jse.getAsJsonObject().getAsJsonPrimitive("messageType");
         if(messageType.getAsInt() != 0)
            continue;

         //
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         int tag = beacon.getMinor();
         // Key is the scannerID + beacon tag
         String key = beacon.getScannerID() + ':' + tag;
         long time = beacon.getTime();

         if(firstTime == 0) {
            firstTime = time;
            bucketStart = time;
            bucketEnd = bucketStart + DURATION;
            Date d0 = new Date(bucketStart);
            Date d1 = new Date(bucketEnd);
            System.out.printf("Processing(%d): %s-%s\n", bucketCount, df.format(d0), df.format(d1));
         }
         // Get accumulation events for the beacon
         HashMap<String, AvgBeacon> beaconRooms = eventWindowByBeacon.get(tag);
         long elapsed = time - bucketStart;
         if (beaconRooms == null) {
            beaconRooms = new HashMap<>();
            eventWindowByBeacon.put(tag, beaconRooms);
         }

         // Get the accumulation bucket for the beacon/room combination
         AvgBeacon bucket = beaconRooms.get(key);
         if(bucket == null) {
            bucket = new AvgBeacon(beacon);
            beaconRooms.put(key, bucket);
         } else if (elapsed <= DURATION) {
            // Average the scanner events into the time window
            bucket.update(beacon);
         } else {
            // Drop duplicate beacon/room events by selecting the room with the largest rssi signal
            for (HashMap<String, AvgBeacon> testRooms : eventWindowByBeacon.values()) {
               Beacon closest = null;
               // Iterate over the rooms that scanned the beacon to select the largest rssi
               for(AvgBeacon avg : testRooms.values()) {
                  Beacon test = avg.getAvgBeacon();
                  if(closest == null) {
                     closest = test;
                     continue;
                  }
                  if(closest.getRssi() < test.getRssi()) {
                     System.out.printf("Dropping %s/%d in favor of %s/%d for %d\n",
                        closest.getScannerID(), closest.getRssi(), test.getScannerID(), test.getRssi(), test.getMinor());
                     closest = test;
                  }
               }
               // Add the event closest to a scanner to the window events
               windows.add(closest);
            }
            eventWindowByBeacon.clear();
            bucketStart = bucketEnd;
            bucketEnd += DURATION;
            bucketCount ++;
            Date d0 = new Date(bucketStart);
            Date d1 = new Date(bucketEnd);
            System.out.printf("Processing(%d): %s-%s\n", bucketCount, df.format(d0), df.format(d1));
         }

      }

      return windows;
   }

   /**
    * Create the room entry/exit events given a list of time ordered events for a single beacon
    * TODO: when replaying events how to identify missing beacon events?
    * @param windowEvents
    */
   static void generateEntryExitEvents(List<Beacon> windowEvents) {
      String lastScanner = "Unknown";

      for(Beacon event : windowEvents) {
         if(!event.getScannerID().equalsIgnoreCase(lastScanner)) {
            System.out.printf("Beacon(%d) exited: %s\n", event.getMinor(), lastScanner);
            lastScanner = event.getScannerID();
            System.out.printf("Beacon(%d) entered: %s\n", event.getMinor(), lastScanner);
         }
      }
   }

   static void dumpToFile(List<Beacon> events, String name) throws IOException {
      FileWriter fw = new FileWriter("/tmp/"+name);
      for(Beacon event : events) {
         fw.write(event.toString());
         fw.write('\n');
      }
      fw.close();
   }
   public static void main(String[] args) throws Exception {
      String dataSet = "FourScannersRun1-2015-03-13.json.gz";
      //String dataSet = "FourScannersBeacon5Run2-2015-03-13.json.gz";
      JsonStreamParser jsp = getDataSetParser(dataSet);
      List<Beacon> windowEvents = averageEvents(jsp);
      Comparator<Beacon> cmp = (o1, o2) -> {
         int compare = o1.getMinor() - o2.getMinor();
         if(compare == 0) {
            compare = (int) (o1.getTime() - o2.getTime());
         }
         return compare;
      };
      Collections.sort(windowEvents, cmp);
      dumpToFile(windowEvents, "averageEvents.log");
      // Break the sequence of events into a map of events by the beacon tag
      HashMap<Integer, List<Beacon>> eventsByBeacon = new HashMap<>();
      windowEvents.forEach(b -> {
            int tag = b.getMinor();
            List<Beacon> events = eventsByBeacon.get(tag);
            if(events == null) {
               events = new ArrayList<Beacon>();
               eventsByBeacon.put(tag, events);
            }
            events.add(b);
         }
      );
      // Now generate exit/entry events for each beacon
      for(List<Beacon> beaconEvents : eventsByBeacon.values()) {
         generateEntryExitEvents(beaconEvents);
      }
   }
}
