package org.jboss.summit2015.beacon.scannerjni;

import org.jboss.logging.Logger;
import org.jboss.summit2015.beacon.common.EventsBucket;
import org.jboss.summit2015.beacon.common.MsgPublisher;
import org.jboss.summit2015.beacon.common.StatusInformation;
import org.jboss.summit2015.scanner.status.StatusProperties;
import org.jboss.summit2015.util.Inet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by starksm on 7/11/15.
 */
public class HealthStatus {
    private static Logger log = Logger.getLogger(HealthStatus.class);

    private MsgPublisher publisher;
    private StatusInformation statusInformation;
    private Thread monitorThread;
    private volatile boolean running;

    static class SystemInfo {
        private long uptime;
        private String loadAverages;
        private long totalram;
        private long freeram;

        public SystemInfo(long uptime, String loadAverages) {
            this.uptime = uptime;
            this.loadAverages = loadAverages;
        }

        public long getUptime() {
            return uptime;
        }

        public void setUptime(long uptime) {
            this.uptime = uptime;
        }

        public String getLoadAverages() {
            return loadAverages;
        }

        public void setLoadAverages(String loadAverages) {
            this.loadAverages = loadAverages;
        }
    }

    public MsgPublisher getPublisher() {
        return publisher;
    }

    public String getStatusQueue() {
        return statusInformation.getStatusQueue();
    }

    public int getStatusInterval() {
        return statusInformation.getStatusInterval();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    /** Begin monitoring in the background, sending status messages to the indicated queue via the publisher
     */
    public void start(MsgPublisher publisher, StatusInformation statusInformation) {
        running = true;
        this.statusInformation = statusInformation;
        this.publisher = publisher;
        monitorThread = new Thread(this::monitorStatus, "HealthStatus");
        log.infof("HealthStatus::start, runnnig with statusInterval: %d\n", statusInformation.getStatusInterval());
    }

    /**
     * Reset any counters
     */
    public void reset() {
    }
    public void stop() {
        running = false;
        monitorThread.interrupt();
    }

    public static void getHostInfo(char hostIPAddress[], char macaddr[]) throws SocketException {
        List<Inet.InterfaceConfig> ifaces = Inet.getAllAddress();
        // Just use the first interface
        for (Inet.InterfaceConfig config : ifaces) {
            if(config.getAddressList().size() > 0) {
                char[] mac = config.getMacaddr().toCharArray();
                System.arraycopy(mac, 0, macaddr, 0, mac.length);
                InterfaceAddress host = config.getAddressList().get(0);
                char[] ip = host.getAddress().getHostAddress().toCharArray();
                System.arraycopy(ip, 0, hostIPAddress, 0, ip.length);
            }
        }
    }

    public static String determineSystemType() {
        return System.getProperty("os.arch");
    }
    public static String determineSystemOS() {
        return System.getProperty("os.name");
    }
    public static SystemInfo getSystemInfo() {
        SystemInfo systemInfo = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process uptimeProc = runtime.exec("uptime");
            BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
            String line = in.readLine();
            long uptime;
            String loadAvgs;
            if (line != null) {
                // 18:25  up 3 days,  9:01, 12 users, load averages: 1.78 2.10 2.23
                // 01:25:06 up 1 day,  7:03,  2 users,  load average: 0.09, 0.16, 0.14
                System.out.printf("Parsing: %s\n", line);
                Pattern parse = Pattern.compile("((\\d+) day[s]?,)?\\s+(\\d+):(\\d+),.*(load average[s]?:.*)");
                Matcher matcher = parse.matcher(line);
                if (matcher.find()) {
                    String _days = matcher.group(2);
                    String _hours = matcher.group(3);
                    String _minutes = matcher.group(4);
                    loadAvgs = matcher.group(5);
                    int days = _days != null ? Integer.parseInt(_days) : 0;
                    int hours = _hours != null ? Integer.parseInt(_hours) : 0;
                    int minutes = _minutes != null ? Integer.parseInt(_minutes) : 0;
                    uptime = (minutes * 60000) + (hours * 60000 * 60) + (days * 6000 * 60 * 24);
                    systemInfo = new SystemInfo(uptime, loadAvgs);
                }
            }
            if(systemInfo != null) {
                long freeram = runtime.freeMemory();
                long totalram = runtime.totalMemory();
                systemInfo.freeram = freeram;
                systemInfo.totalram = totalram;
            }
        } catch (IOException e) {
            log.warn("Failed to get uptime", e);
        }
        return systemInfo;
    }

    protected void monitorStatus() {
        int statusInterval = statusInformation.getStatusInterval();
        String statusQueue = statusInformation.getStatusQueue();
        String scannerID = statusInformation.getScannerID();
        Properties statusProperties = new Properties();
        String ScannerID = StatusProperties.ScannerID.name();
        String HostIPAddress = StatusProperties.HostIPAddress.name();
        String MACAddress = StatusProperties.MACAddress.name();
        String SystemType = StatusProperties.SystemType.name();
        String SystemOS = StatusProperties.SystemOS.name();
        String SystemTime = StatusProperties.SystemTime.name();
        String SystemTimeMS = StatusProperties.SystemTimeMS.name();
        String Uptime = StatusProperties.Uptime.name();
        String SystemUptime = StatusProperties.SystemUptime.name();
        String LoadAverage = StatusProperties.LoadAverage.name();
        String Procs = StatusProperties.Procs.name();
        String RawEventCount = StatusProperties.RawEventCount.name();
        String PublishEventCount = StatusProperties.PublishEventCount.name();
        String HeartbeatCount = StatusProperties.HeartbeatCount.name();
        String HeartbeatRSSI = StatusProperties.HeartbeatRSSI.name();
        String EventsWindow = StatusProperties.EventsWindow.name();
        String ActiveBeacons = StatusProperties.ActiveBeacons.name();
        String MemTotal = StatusProperties.MemTotal.name();
        String MemFree = StatusProperties.MemFree.name();
        String MemActive = StatusProperties.MemActive.name();
        String SwapTotal = StatusProperties.SwapTotal.name();
        String SwapFree = StatusProperties.SwapFree.name();

        // Determine the scanner type
        String systemType = determineSystemType();
        log.infof("Determined SystemType as: %s\n", systemType);
        String systemOS = determineSystemOS();
        log.infof("Determined SystemOS as: %s\n", systemOS);

        SystemInfo beginInfo = getSystemInfo();
// Send an initial hello status msg with the host inet address
        char hostIPAddress[] = new char[128];
        char macaddr[] = new char[32];
        try {
            getHostInfo(hostIPAddress, macaddr);
        } catch (SocketException e) {
            log.warn("Failed to read host address info", e);
        }

        while(running) {
            statusProperties.put(ScannerID, scannerID);
            statusProperties.put(HostIPAddress, hostIPAddress);
            statusProperties.put(MACAddress, macaddr);
            statusProperties.put(SystemType, systemType);
            statusProperties.put(SystemOS, systemOS);

            // Time
            long ms = System.currentTimeMillis();
            String timestr = new Date(ms).toString();
            statusProperties.put(SystemTime, timestr);
            statusProperties.put(SystemTimeMS, ""+ms);
            log.infof("--- HealthStatus: %s\n", timestr);

            // Get the load average
            SystemInfo info = getSystemInfo();
            // Create the status message properties
            statusProperties.put(LoadAverage, info.getLoadAverages());
            statusProperties.put(RawEventCount, ""+statusInformation.getRawEventCount());
            statusProperties.put(PublishEventCount, ""+statusInformation.getPublishEventCount());
            statusProperties.put(HeartbeatCount, ""+statusInformation.getHeartbeatCount());
            statusProperties.put(HeartbeatRSSI, ""+statusInformation.getHeartbeatRSSI());
            log.infof("RawEventCount: %d, PublishEventCount: %d, HeartbeatCount: %d, HeartbeatRSSI: %d\n", statusInformation.getRawEventCount(),
                statusInformation.getPublishEventCount(), statusInformation.getHeartbeatCount(), statusInformation.getHeartbeatRSSI());

            // Events bucket info
            EventsBucket eventsBucket = statusInformation.getStatusWindow();
            if(eventsBucket != null) {
                StringBuilder eventsBucketStr = new StringBuilder();
                eventsBucket.toSimpleString(eventsBucketStr);
                statusProperties.put(EventsWindow, eventsBucketStr.toString());
                log.infof("EventsBucket[%ld]: %s\n", eventsBucket.size(), eventsBucketStr.toString());
                statusProperties.put(ActiveBeacons, ""+eventsBucket.size());
            }

            // System uptime, load, procs, memory info
            int mb = 1024*1024;
            long uptimeDiff = info.uptime - beginInfo.uptime;
            long days = uptimeDiff / (24*3600);
            long hours = (uptimeDiff - days * 24*3600) / 3600;
            long minute = (uptimeDiff - days * 24*3600 - hours*3600) / 60;
            long seconds = uptimeDiff - days * 24*3600 - hours*3600 - minute*60;
            String uptime = String.format("uptime: %d, days:%d, hrs:%d, min:%d, sec:%d", uptimeDiff, days, hours, minute, seconds);
            statusProperties.put(Uptime, uptime);
            log.infof("Scanner %s", uptime);
            // Calcualte system uptime
            uptimeDiff = info.uptime;
            days = uptimeDiff / (24*3600);
            hours = (uptimeDiff - days * 24*3600) / 3600;
            minute = (uptimeDiff - days * 24*3600 - hours*3600) / 60;
            seconds = uptimeDiff - days * 24*3600 - hours*3600 - minute*60;
            uptime = String.format("uptime: %d, days:%d, hrs:%d, min:%d, sec:%d", uptimeDiff, days, hours, minute, seconds);
            statusProperties.put(SystemUptime, uptime);
            log.infof("System %s\n", uptime);

            log.infof("loadavg: %s\n", info.loadAverages);
            statusProperties.put(LoadAverage, info.loadAverages);

            statusProperties.put(MemTotal, ""+info.totalram);
            statusProperties.put(MemActive, ""+(info.totalram - info.freeram));
            statusProperties.put(MemFree, ""+info.freeram);
            log.infof("MemTotal: %d;  MemFree: %d\n", info.totalram, info.freeram);

            // Publish the status
            statusInformation.setLastStatus(statusProperties);

            // Wait for statusInterval before next status message
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(statusInterval));
            } catch (InterruptedException e) {
                log.warn("Exiting on InterruptedException");
                running = false;
            }
        }
    }

}