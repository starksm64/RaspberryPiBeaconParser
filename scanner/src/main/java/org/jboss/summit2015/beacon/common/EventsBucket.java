package org.jboss.summit2015.beacon.common;

import org.jboss.summit2015.beacon.bluez.BeaconInfo;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Snapshot of BeaconInfo events into a time window
 */
public class EventsBucket {
    long bucketStart;
    long bucketEnd;
    int eventCount;
    Map<Integer, BeaconInfo> bucket;

    EventsBucket(Map<Integer, BeaconInfo> bucket, int eventCount, long start, long end) {
        this.eventCount = eventCount;
        this.bucket = new HashMap<>(bucket);
        this.bucketStart = start;
        this.bucketEnd = end;
    }

    public Map<Integer, BeaconInfo> getBucket() {
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
        output.append(start.toString());
        output.append('-');
        output.append(end.toString());
    }

    /**
     * The BeaconInfo counts string with a leading timestamp
     */
    public void toString(StringBuilder output) {
        Date start = new Date(bucketStart);
        // Report the stats for this time window and then reset
        long width = bucketEnd - bucketStart;
        String msg = String.format("+++ BeaconInfo counts for window(%d,%d): %s\n", size(), width, start.toString());
        output.append(msg);
        toSimpleString(output);
    }
    /**
     * Just the BeaconInfo counts string
     */
    public void toSimpleString(StringBuilder output) {
        for(Map.Entry<Integer,BeaconInfo> iter : bucket.entrySet()) {
            String msg = String.format("+%d=%d; ", iter.getKey(), iter.getValue().getCount());
            output.append(msg);
        }
    }
}
