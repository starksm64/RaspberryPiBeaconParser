package org.jboss.summit2015.scanner.status.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.Map;

/**
 * Created by starksm on 5/28/15.
 */
public class ScannerInfo implements Comparable {
    /** Map of the last published heartbeat message properties */
    private Map<String, String> lastStatus;
    /** The scanner id */
    private SimpleStringProperty scannerID;
    /** The number of messages the scanner has sent to broker */
    private int publishCount;
    /** The scanner system time of the last heartbeat */
    private long time;
    /** The elapsed time in MS since the last heartbeat according to the JVM time */
    private long sinceLastHeartbeat;

    public ScannerInfo(long time, int publishCount, Map<String, String> lastStatus) {
        this(lastStatus.get("ScannerID").toString(), time, publishCount, lastStatus);
    }

    public ScannerInfo(String scannerID, long time, int publishCount, Map<String, String> lastStatus) {
        this.scannerID = new SimpleStringProperty(scannerID);
        this.time = time;
        this.publishCount = publishCount;
        this.lastStatus = lastStatus;
    }

    @Override
    public int compareTo(Object o) {
        ScannerInfo other = (ScannerInfo) o;
        return getScannerID().compareTo(other.getScannerID());
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }
    @Override
    public int hashCode() {
        return getScannerID().hashCode();
    }

    public String getScannerID() {
        return scannerID.get();
    }

    public SimpleStringProperty scannerIDProperty() {
        return scannerID;
    }

    public void setScannerID(String scannerID) {
        this.scannerID.set(scannerID);
    }

    public Map<String, String> getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(Map<String, String> lastStatus) {
        this.lastStatus = lastStatus;
    }


    public String getProperty(String key) {
        return lastStatus.get(key);
    }
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    public long getSinceLastHeartbeat() {
        return sinceLastHeartbeat;
    }

    public void setSinceLastHeartbeat(long sinceLastHeartbeat) {
        this.sinceLastHeartbeat = sinceLastHeartbeat;
    }

    public int getPublishCount() {
        return publishCount;
    }

    public void setPublishCount(int publishCount) {
        this.publishCount = publishCount;
    }

    public String toString() {
        return String.format("ID[%s]: %d,%d", getScannerID(), time, publishCount);
    }

    public String toString(long now) {
        return String.format("ID[%s]: %d,%d", getScannerID(), (now - time) / 1000, publishCount);
    }
}
