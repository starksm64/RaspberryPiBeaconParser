package org.jboss.summit2015.beacon.scannerjni;

import org.jboss.logging.Logger;
import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.beacon.bluez.BeaconInfo;
import org.jboss.summit2015.beacon.common.EventsBucket;
import org.jboss.summit2015.beacon.common.MsgPublisher;
import org.jboss.summit2015.beacon.common.ParseCommand;
import org.jboss.summit2015.beacon.common.StatusInformation;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Called for non-null beacon_info events by the thread loop
 */
public class BeaconEventConsumer {
    private static Logger log = Logger.getLogger(BeaconEventConsumer.class);

    private volatile boolean running;
    /** The count of messages to send in batches if > 0 */
    private int batchCount;
    /** vector of beacon events when sending events in batchCount transactions to the broker */
    private List<Beacon> events;
    private MsgPublisher publisher;
    private ConcurrentLinkedDeque<EventsBucket> exchanger;
    /** An information class task holding published event counts */
    private StatusInformation statusInformation;
    private ParseCommand parseCommand;

    void handleMessage(EventsBucket bucket) {
        if(log.isTraceEnabled())
            log.tracef("handleMessage(%d)\n", bucket.getEventCount());
        for(BeaconInfo beacon : bucket.getBucket().values()) {
            int publishID = statusInformation.updatePublishEventCount();
            beacon.setScannerSequenceNo(publishID);
            if(beacon.isHeartbeat()) {
                if(!parseCommand.skipHeartbeat)
                    publisher.publishStatus(beacon);
            } else {
                publisher.publish(null, beacon);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    int getBatchCount() {
        return batchCount;
    }
    void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    /**
     *
     */
    public void publishEvents() {
        running = true;
        System.out.printf("BeaconEventConsumer::publishEvents, starting\n");
        while (running) {
            EventsBucket info = exchanger.pollFirst();
            if(statusInformation.isStatusUpdated()) {
                Properties lastStatus = statusInformation.getLastStatus();
                publisher.publishProperties(statusInformation.getStatusQueue(), lastStatus);
                statusInformation.clearStatusChanged();
            }
            else if (info != null) {
                handleMessage(info);
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.printf("BeaconEventConsumer::publishEvents, exiting\n");
    }

    public ParseCommand getParseCommand() {
        return parseCommand;
    }

    public void setParseCommand(ParseCommand parseCommand) {
        this.parseCommand = parseCommand;
    }

    public void init(ConcurrentLinkedDeque<EventsBucket> exchanger, MsgPublisher msgPublisher, StatusInformation statusInformation) {
        this.exchanger = exchanger;
        this.publisher = msgPublisher;
        this.statusInformation = statusInformation;
    }
}
