package org.jboss.summit2015.beacon.common;

import org.jboss.summit2015.beacon.bluez.BeaconInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents a collection of Beacon events seen within a time window
 */
public class EventsWindow {
    int windowSizeSeconds;
    // Current analyze window begin/end in milliseconds to be compatible with BeaconInfo.time
    long begin;
    long end;
    int eventCount;
    Map<Integer, BeaconInfo> eventsMap = new HashMap<>();

    public int getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public Map<Integer, BeaconInfo>  getEventsMap() {
        return eventsMap;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void reset(int sizeInSeconds) {
        windowSizeSeconds = sizeInSeconds;
        eventsMap.clear();
        resetCurrentBucket();
    }
    public EventsBucket getCurrentBucket() {
        // Calculate the bucket averages
        for(BeaconInfo info : eventsMap.values()) {
            int count = info.count;
            info.rssi = (info.rssi / count);
            info.time = (info.time / count);
        }
        // Copy the current events and return it
        EventsBucket window = new EventsBucket(eventsMap, eventCount, begin, end);
        // Clear the current event Map
        eventsMap.clear();
        begin = end;
        end += 1000*windowSizeSeconds;
        eventCount = 0;
        return window;
    }
    public EventsBucket addEvent(BeaconInfo info, boolean isHeartbeat) {
        EventsBucket window = null;
        if(info.time < end) {
            // Update the beacon event counts
            addInfo(eventsMap, info, isHeartbeat);
            eventCount ++;
        } else {
            // Calculate the bucket averages
            for(BeaconInfo avg : eventsMap.values()) {
                int count = avg.count;
                avg.rssi = (avg.rssi / count);
                avg.time = (avg.time / count);
            }
            // Copy the current events and return it
            window = new EventsBucket(eventsMap, eventCount, begin, end);
            // Clear the current event Map
            eventsMap.clear();
            begin = end;
            end += 1000*windowSizeSeconds;
            if(end < info.time) {
                // Warn about this as it seems to happen
                System.err.printf("Warn: next bucket end(%d) < info.time(%d)\n", end, info.time);
                resetCurrentBucket();
            }
            eventCount = 0;
            // Add the event to the next window
            addInfo(eventsMap, info, isHeartbeat);
            eventCount ++;
        }
        return window;
    }

    private void addInfo(Map<Integer, BeaconInfo> eventsMap,  BeaconInfo info, boolean isHeartbeat) {
        if(!eventsMap.containsKey(info.getMinor())) {
            eventsMap.put(info.getMinor(), info);
            info.count = 1;
            info.isHeartbeat = isHeartbeat;
        } else {
            BeaconInfo windowInfo = eventsMap.get(info.getMinor());
            windowInfo.rssi = (windowInfo.getRssi() + info.getRssi());
            windowInfo.time = (windowInfo.getTime() + info.getTime());
            windowInfo.count ++;
        }
    }
    private void resetCurrentBucket() {
        begin = System.currentTimeMillis();
        end = begin + 1000*windowSizeSeconds;
    }
}
