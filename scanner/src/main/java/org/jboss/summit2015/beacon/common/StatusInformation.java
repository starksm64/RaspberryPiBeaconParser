package org.jboss.summit2015.beacon.common;

import org.jboss.summit2015.beacon.bluez.BeaconInfo;

import java.util.Properties;

/**
 * Created by starksm on 7/10/15.
 */
public class StatusInformation {
    private String scannerID;
    private String statusQueue;
    private String bcastAddress;
    private int bcastPort = 12345;
    private int statusInterval;
    private int rawEventCount;
    private int publishEventCount;
    private int heartbeatCount;
    private EventsWindow statusWindow = new EventsWindow();
    private EventsBucket lastWindow;
    private SMA heartbeatRSSI;
    Properties lastStatus;
    private volatile boolean statusUpdated;

    public String getScannerID() {
        return scannerID;
    }

    public void setScannerID(String scannerID) {
        this.scannerID = scannerID;
    }

    public String getStatusQueue() {
        return statusQueue;
    }

    public void setStatusQueue(String statusQueue) {
        this.statusQueue = statusQueue;
    }

    public String getBcastAddress() {
        return bcastAddress;
    }

    public void setBcastAddress(String bcastAddress) {
        this.bcastAddress = bcastAddress;
    }

    public int getBcastPort() {
        return bcastPort;
    }

    public void setBcastPort(int bcastPort) {
        this.bcastPort = bcastPort;
    }

    public int getStatusInterval() {
        return statusInterval;
    }

    public void setStatusInterval(int statusInterval) {
        this.statusInterval = statusInterval;
        statusWindow.reset(statusInterval);
    }

    public int getRawEventCount() {
        return rawEventCount;
    }

    public void setRawEventCount(int rawEventCount) {
        this.rawEventCount = rawEventCount;
    }

    public int getPublishEventCount() {
        return publishEventCount;
    }

    public void setPublishEventCount(int publishEventCount) {
        this.publishEventCount = publishEventCount;
    }
    public int updatePublishEventCount() {
        this.publishEventCount ++;
        return this.publishEventCount;
    }

    public int getHeartbeatCount() {
        return heartbeatCount;
    }

    public void setHeartbeatCount(int heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }

    public EventsBucket getStatusWindow() {
        return lastWindow;
    }

    public void setStatusWindow(EventsWindow statusWindow) {
        this.statusWindow = statusWindow;
    }

    public EventsBucket getLastWindow() {
        return lastWindow;
    }

    public void setLastWindow(EventsBucket lastWindow) {
        this.lastWindow = lastWindow;
    }

    public SMA getHeartbeatRSSI() {
        return heartbeatRSSI;
    }

    public void setHeartbeatRSSI(SMA heartbeatRSSI) {
        this.heartbeatRSSI = heartbeatRSSI;
    }

    public Properties getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(Properties lastStatus) {
        this.lastStatus = lastStatus;
    }

    public boolean isStatusUpdated() {
        return statusUpdated;
    }

    public void setStatusUpdated(boolean statusUpdated) {
        this.statusUpdated = statusUpdated;
    }

    public void clearStatusChanged() {
        this.statusUpdated = false;
    }

    public void addEvent(BeaconInfo info, boolean isHeartbeat) {
        rawEventCount ++;
        if(isHeartbeat) {
            heartbeatCount ++;
            heartbeatRSSI.add(info.rssi);
        }
        EventsBucket window = statusWindow.addEvent(info, isHeartbeat);
        if(window != null)
            lastWindow = window;
    }
}
