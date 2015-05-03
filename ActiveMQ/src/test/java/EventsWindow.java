import org.jboss.summit2015.beacon.Beacon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2015 Red Hat Inc.
 */
public class EventsWindow {
   int windowSizeSeconds;
   // Current analyze window begin/end in milliseconds to be compatible with Beacon.time
   long begin;
   long end;
   int eventCount;
   int traceID;
   HashMap<Integer, HashMap<String, AvgBeacon>> eventWindowByBeacon = new HashMap<>();

   EventsWindow() {
      reset(5);
   }

   public void reset(int sizeInSeconds) {
      windowSizeSeconds = sizeInSeconds;
      begin = System.currentTimeMillis();
      end = begin + 1000 * windowSizeSeconds;
      eventWindowByBeacon.clear();
   }

   public int getTraceID() {
      return traceID;
   }
   public void setTraceID(int traceID) {
      this.traceID = traceID;
   }

   private void addInfo(Beacon info) {
      // Get accumulation events for the beacon
      int tag = info.getMinor();
      HashMap<String, AvgBeacon> beaconRooms = eventWindowByBeacon.get(tag);
      // Key is the scannerID + beacon tag
      String scannerID = info.getScannerID();
      if(scannerID.endsWith("x")) {
         scannerID = scannerID.substring(0, scannerID.length() - 1);
         info.setScannerID(scannerID);
      }
      String key = scannerID + ':' + tag;
      if (beaconRooms == null) {
         beaconRooms = new HashMap<>();
         eventWindowByBeacon.put(tag, beaconRooms);
         if(tag == traceID)
            System.out.printf("+++ NewLocation: %s\n", key);
      }

      // Get the accumulation bucket for the beacon/room combination
      AvgBeacon bucket = beaconRooms.get(key);
      if (bucket == null) {
         bucket = new AvgBeacon(info);
         beaconRooms.put(key, bucket);
      } else {
         // Average the scanner events into the time window
         bucket.update(info);
      }
      if(tag == traceID)
         System.out.printf("+++ Avg[%d]=%d\n", tag, bucket.getAvgRssi());
   }

   public List<Beacon> addEvent(Beacon info) {
      ArrayList<Beacon> windows = null;

      if (info.getTime() < end) {
         // Update the beacon event counts
         addInfo(info);
         eventCount++;
      } else {
         windows = new ArrayList<>();
         // Drop duplicate beacon/room events by selecting the room with the largest rssi signal
         for (HashMap<String, AvgBeacon> testRooms : eventWindowByBeacon.values()) {
            Beacon closest = null;
            // Iterate over the rooms that scanned the beacon to select the largest rssi
            for (AvgBeacon avg : testRooms.values()) {
               Beacon test = avg.getAvgBeacon();
               if (closest == null) {
                  closest = test;
                  if(traceID == test.getMinor())
                     System.out.printf("+++ Closest[%d] = %s/%d\n", traceID, closest.getScannerID(), closest.getRssi());
                  continue;
               }
               if(traceID == test.getMinor())
                  System.out.printf("+++ Testing[%d]: %s/%d against %s/%d\n", traceID, closest.getScannerID(), closest.getRssi(), test.getScannerID(), test.getRssi());
               if (closest.getRssi() < test.getRssi()) {
                  System.out.printf("Dropping %s/%d in favor of %s/%d for %d\n",
                     closest.getScannerID(), closest.getRssi(), test.getScannerID(), test.getRssi(), test.getMinor());
                  closest = test;
               }
            }
            // Add the event closest to a scanner to the window events
            windows.add(closest);
         }

         // Clear the current event map
         eventWindowByBeacon.clear();
         begin = end;
         end += 1000 * windowSizeSeconds;
         eventCount = 0;
         // Add the event to the next window
         addInfo(info);
         eventCount++;
      }
      return windows;
   }
}
