package org.jboss.summit2015.beacon.scannerjni;

import org.jboss.logging.Logger;
import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.beacon.MsgType;
import org.jboss.summit2015.beacon.bluez.BeaconInfo;
import org.jboss.summit2015.beacon.common.EventsBucket;
import org.jboss.summit2015.beacon.common.EventsWindow;
import org.jboss.summit2015.beacon.common.MsgPublisher;
import org.jboss.summit2015.beacon.common.MsgPublisherFactory;
import org.jboss.summit2015.beacon.common.ParseCommand;
import org.jboss.summit2015.beacon.common.StatusInformation;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by starksm on 7/11/15.
 */
public class HCIDumpParser {
    private static Logger log = Logger.getLogger(HCIDumpParser.class);
    private static String STOP_MARKER_FILE = "/var/run/scannerd.STOP";

    /** Command line argument information */
    private ParseCommand parseCommand;
    /** The interface for the messaging layer publisher */
    private MsgPublisher publisher;
    /** The thread for the publishing beacon_info events via the MsgPublisher */
    private Thread consumerThread;
    /** The beacon_info event consumer class running in background */
    private BeaconEventConsumer eventConsumer = new BeaconEventConsumer();
    /** Shared queue for producer/consumer message exchange */
    private ConcurrentLinkedDeque<EventsBucket> eventExchanger = new ConcurrentLinkedDeque<>();
    /** An information class published by the HealthStatus task */
    private StatusInformation statusInformation = new StatusInformation();
    /** The ScannerView implementation used to output information about the scanner */
    private ScannerView scannerView;
    /** A background status monitor task class */
    private HealthStatus statusMonitor = new HealthStatus();
    /** The time window of collected beacon_info events */
    private EventsWindow timeWindow = new EventsWindow();
    private volatile boolean stopped;
    private int lastRSSI[] = new int[10];
    private int heartbeatCount = 0;
    private long eventCount = 0;
    private long maxEventCount = 0;
    private long lastMarkerCheckTime = 0;

    public HCIDumpParser(ParseCommand cmdArgs) {
        this.parseCommand = cmdArgs;
    }

    public void start() throws Exception {
        stopped = false;
        eventConsumer.setParseCommand(parseCommand);
        String clientID = parseCommand.clientID;
        if (clientID.isEmpty())
            clientID = parseCommand.scannerID;
        timeWindow.reset(parseCommand.analyzeWindow);
        // Setup the status information
        statusInformation.setScannerID(parseCommand.getScannerID());
        statusInformation.setStatusInterval(parseCommand.statusInterval);
        statusInformation.setStatusQueue(parseCommand.getStatusDestinationName());
        statusInformation.setBcastAddress(parseCommand.getBcastAddress());
        statusInformation.setBcastPort(parseCommand.getBcastPort());

        if (parseCommand.isAnalyzeMode()) {
            log.infof("Running in analyze mode, window=%d seconds, begin=%d\n", parseCommand.getAnalyzeWindow(),
                timeWindow.getBegin());
        }
        else if (!parseCommand.isSkipPublish()) {
            String username = parseCommand.getUsername();
            String password = parseCommand.getPassword();
            publisher = MsgPublisherFactory.newMsgPublisher(parseCommand.getPubType(), parseCommand.getBrokerURL(), clientID, username, password);
            publisher.setUseTopics(!parseCommand.isUseQueues());
            log.infof("setUseTopics: %s\n", publisher.isUseTopics() ? "true" : "false");
            publisher.setDestinationName(parseCommand.getDestinationName());
            if (parseCommand.batchCount > 0) {
                publisher.setUseTransactions(true);
                log.infof("Enabled transactions\n");
            }
            publisher.start(parseCommand.isAsyncMode());

            // Create a thread for the consumer unless running in battery test mode
            if(!parseCommand.isBatteryTestMode()) {
                eventConsumer.init(eventExchanger, publisher, statusInformation);
                consumerThread = new Thread(eventConsumer::publishEvents, "BeaconEventConsumer");
                consumerThread.setDaemon(true);
                consumerThread.start();
                log.infof("Started event consumer thread\n");
            }
        }
        else {
            log.infof("Skipping publish of parsed beacons\n");
        }

        // If the status interval is > 0, start the health status monitor
        if(parseCommand.getStatusInterval() > 0) {
            statusMonitor.start(publisher, statusInformation);
        }

    }

    public void stop() throws Exception {
        stopped = true;
        if(consumerThread != null)
            consumerThread.interrupt();
        if(publisher != null)
            publisher.stop();
        statusMonitor.stop();
    }

    /**
     * Callback from native stack
     * @param rawInfo - the raw direct ByteBuffer store shared with native stack
     */
    public boolean beaconEvent(ByteBuffer rawInfo) {
        // First get a read only ByteBuffer view for efficient testing of the event info
        BeaconInfo info = new BeaconInfo(rawInfo);
        eventCount ++;
        if(log.isTraceEnabled()) {
            log.tracef("beaconEvent(), uuid=%s, major=%d, minor=%d, rssi=%d\n",
                info.uuid, info.major, info.minor, info.rssi);
        }

        // Check for a termination marker every 1000 events or 5 seconds
        boolean stop = false;
        long elapsed = info.time - lastMarkerCheckTime;
        if((eventCount % 1000) == 0 || elapsed > 5000) {
            lastMarkerCheckTime = info.time;
            stop = stopMarkerExists();
            log.infof("beaconEvent(time=%d), status eventCount=%d, stop=%b\n", info.time, eventCount, stop);
        }
        // Check max event count limit
        if(maxEventCount > 0 && eventCount >= maxEventCount)
            stop = true;

        // Check for heartbeat
        boolean isHeartbeat = parseCommand.heartbeatUUID.compareTo(info.uuid) == 0;
        if(parseCommand.isBatteryTestMode()) {
            // Send the raw unaveraged heartbeat info, or ignore non-heartbeat events
            if(isHeartbeat)
                sendRawHeartbeat(info);
        } else {
            // Merge the event into the current time window
            EventsBucket bucket = timeWindow.addEvent(info, isHeartbeat);
            statusInformation.addEvent(info, isHeartbeat);
            // Now handle the bucket if a new one has been created
            if (bucket != null) {
                if (!parseCommand.isSkipPublish()) {
                    eventExchanger.offerLast(bucket);
                } else {
                    if (parseCommand.isAnalyzeMode()) {
                        printBeaconCounts(bucket);
                    } else {
                        if (!isHeartbeat || (isHeartbeat && !parseCommand.isSkipHeartbeat()))
                            printBeaconCounts(info, bucket);
                    }
                }
                // Display either the closest beacon or status
                if (scannerView != null) {
                    if (scannerView.isDisplayBeaconsMode())
                        displayClosestBeacon(bucket);
                    else
                        displayStatus();
                }
            }
        }
        // If stop is true, notify any callers in waitForStop
        if(stop) {
            synchronized (this) {
                stopped = true;
                this.notifyAll();
            }
        }
        return stop;
    }

    /**
     *
     */
    public synchronized void waitForStop() {
        try {
            while(stopped == false)
                this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    boolean stopMarkerExists() {
        File test = new File(STOP_MARKER_FILE);
        boolean stop = test.exists();
        if(stop) {
            log.info("Found STOP marker file, will exit...");
        }
        return stop;
    }

    void printBeaconCounts(BeaconInfo beacon, EventsBucket bucket) {
        log.infof("Window: parsed(%s):\n", beacon.toString());
        printBeaconCounts(bucket);
    }
    void printBeaconCounts(EventsBucket bucket) {
        StringBuilder tmp = new StringBuilder();
        bucket.toString(tmp);
        log.infof("%s\n", tmp.toString());
    }

    void sendRawHeartbeat(BeaconInfo info) {
        Beacon beacon = new Beacon(parseCommand.getScannerID(), info.uuid, info.code, info.manufacturer, info.major, info.minor,
            info.power, info.rssi, info.time);
        beacon.setMessageType(MsgType.SCANNER_HEARTBEAT.ordinal());
        publisher.publishStatus(beacon);
        lastRSSI[heartbeatCount%10] = info.rssi;
        heartbeatCount ++;
        if(heartbeatCount % 100 == 0) {
            log.infof("Last[10].RSSI:");
            for(int n = 0; n < 10; n ++)
                log.infof("%d,", lastRSSI[n]);
            log.infof("\n");
        }
    }
    void displayClosestBeacon(EventsBucket bucket) {
        /* TODO
        map<int32_t, beacon_info>::const_iterator iter = bucket->begin();
        int32_t maxRSSI = -100;
        const beacon_info *closest = nullptr;
        const beacon_info *heartbeat = nullptr;
        while (iter != bucket->end()) {
            // Skip the heartbeast beacon...
            if(iter->second.rssi > maxRSSI) {
                if(scannerUUID.compare(iter->second.uuid) == 0)
                    heartbeat = &iter->second;
                else {
                    maxRSSI = iter->second.rssi;
                    closest = &iter->second;
                }
            }
            iter++;
        }
        if(closest != nullptr) {
            Beacon closestBeacon(parseCommand.getScannerID(), closest->uuid, closest->code, closest->manufacturer,
                closest->major, closest->minor, closest->power, closest->calibrated_power,
                closest->rssi, closest->time);
            scannerView->displayBeacon(closestBeacon);
        } else if(heartbeat != nullptr) {
            // The only beacon seen was the heartbeat beacon, so display it
            Beacon heartbeatBeacon(parseCommand.getScannerID(), heartbeat->uuid, heartbeat->code, heartbeat->manufacturer,
                heartbeat->major, heartbeat->minor, heartbeat->power, heartbeat->calibrated_power,
                heartbeat->rssi, heartbeat->time);
            scannerView->displayHeartbeat(heartbeatBeacon);
        }
        */
    }
    void displayStatus() {
        scannerView.displayStatus(statusInformation);
    }

}
