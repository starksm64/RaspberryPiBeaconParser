import org.jboss.summit2015.beacon.Beacon;

import java.util.Date;
import java.util.Map;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class EventsBucket {
   private long bucketStart;
   private long bucketEnd;
   private int eventCount;
   private Map<Integer, Beacon> bucket;

   public EventsBucket(Map<Integer, Beacon> bucket, int eventCount, long bucketStart, long bucketEnd) {
      this.bucketStart = bucketStart;
      this.bucketEnd = bucketEnd;
      this.eventCount = eventCount;
      this.bucket = bucket;
   }

   public Map<Integer, Beacon> getEvents() {
      return bucket;
   }
   public int getEventCount() {
      return eventCount;
   }

   public void setEventCount(int eventCount) {
      this.eventCount = eventCount;
   }


   public long getBucketStart() {
      return bucketStart;
   }

   public void setBucketStart(long bucketStart) {
      this.bucketStart = bucketStart;
   }

   public long getBucketEnd() {
      return bucketEnd;
   }

   public void setBucketEnd(long bucketEnd) {
      this.bucketEnd = bucketEnd;
   }

   public long size() {
      return bucket.size();
   }

   public void toTimeWindowString(StringBuilder output) {
      Date start = new Date(bucketStart);
      Date end = new Date(bucketEnd);
      output.append(start);
      output.append('-');
      output.append(end);
   }

   /**
    * The beacon counts string with a leading timestamp
    */
   public void toString(StringBuilder output) {
      Date start = new Date(bucketStart);
      // Report the stats for this time window and then reset
      long width = bucketEnd - bucketStart;
      String prefix = String.format("+++ Beacon counts for window(%d,%d): %s\n", size(), width, start);
      output.append(prefix);
      toSimpleString(output);
   }

   /**
    * Just the beacon counts string
    */
   void toSimpleString(StringBuilder output) {
      bucket.forEach((key, beacon) -> {
         String entry = String.format("+%d=%d; ", key, beacon.getMinor());
         output.append(entry);
      });
   }
}
