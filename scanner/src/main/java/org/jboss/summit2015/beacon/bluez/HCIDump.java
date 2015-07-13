package org.jboss.summit2015.beacon.bluez;

import org.jboss.summit2015.beacon.Beacon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An integration class
 */
public class HCIDump {
    static final int beacon_info_SIZEOF = 80;
    static final int UUID_OFFSET = 0;
    static final int IS_HEARTBEAT_OFFSET = 36;
    static final int count_OFFSET = 40;
    static final int code_OFFSET = 44;
    static final int manufacturer_OFFSET = 48;
    static final int major_OFFSET = 52;
    static final int minor_OFFSET = 56;
    static final int power_OFFSET = 60;
    static final int calibrated_power_OFFSET = 64;
    static final int rssi_OFFSET = 68;
    static final int time_OFFSET = 72;
    /*
        typedef struct beacon_info {
            char uuid[36];
            bool isHeartbeat;
            int32_t count;
            int32_t code;
            int32_t manufacturer;
            int32_t major;
            int32_t minor;
            int32_t power;
            int32_t calibrated_power;
            int32_t rssi;
            int64_t time;
        } beacon_info;
        root@debian8x64:~/NativeRaspberryPiBeaconParser# Debug/tests/testBeaconBuffer
        sizeof(beacon_info) = 80
        offsetof(beacon_info.isHeartbeat) = 36
        offsetof(beacon_info.count) = 40
        offsetof(beacon_info.code) = 44
        offsetof(beacon_info.manufacturer) = 48
        offsetof(beacon_info.major) = 52
        offsetof(beacon_info.minor) = 56
        offsetof(beacon_info.power) = 60
        offsetof(beacon_info.calibrated_power) = 64
        offsetof(beacon_info.rssi) = 68
        offsetof(beacon_info.time) = 72
     */
    private static ByteBuffer theNativeBuffer;
    private static volatile int eventCount = 0;
    private static IRawEventCallback rawEventCallback;
    private static IEventCallback eventCallback;
    private static String scannerID;

    public native static void allocScanner(ByteBuffer bb, int device);
    public native static void freeScanner();

    public static IRawEventCallback getRawEventCallback() {
        return rawEventCallback;
    }

    public static void setRawEventCallback(IRawEventCallback rawEventCallback) {
        HCIDump.rawEventCallback = rawEventCallback;
    }

    public static IEventCallback getEventCallback() {
        return eventCallback;
    }

    public static void setEventCallback(IEventCallback eventCallback) {
        HCIDump.eventCallback = eventCallback;
    }

    public static String getScannerID() {
        return scannerID;
    }

    public static void setScannerID(String scannerID) {
        HCIDump.scannerID = scannerID;
    }

    /**
     * Setup the native scanner stack for the given hciDev interface. This allocates the direct ByteBuffer used by
     * the native stack and starts the scanner running by calling allocScanner
     * @param hciDev - the host controller interface (for example, hci0)
     * @see #allocScanner(ByteBuffer, int)
     */
    public static void initScanner(String hciDev) {
        char devNumber = hciDev.charAt(hciDev.length()-1);
        int device = devNumber - '0';
        ByteBuffer bb = ByteBuffer.allocateDirect(beacon_info_SIZEOF);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        HCIDump.theNativeBuffer = bb;
        HCIDump.allocScanner(bb, device);
    }

    /**
     *
     * @param info
     * @param buffer
     */
    public static void freezeBeaconInfo(BeaconInfo info, ByteBuffer buffer) {
        byte uuid[] = new byte[36];
        int uuidLength = 0;
        for (int n = 0; n < uuid.length; n++) {
            byte bn = buffer.get(n);
            if(bn == 0)
                break;
            uuid[n] = bn;
            uuidLength ++;
        }
        info.uuid = new String(uuid, 0, uuidLength);
        info.isHeartbeat = buffer.getInt(IS_HEARTBEAT_OFFSET) != 0;
        info.count = buffer.getInt(count_OFFSET);
        info.code = buffer.getInt(code_OFFSET);
        info.manufacturer = buffer.getInt(manufacturer_OFFSET);
        info.major = buffer.getInt(major_OFFSET);
        info.minor = buffer.getInt(minor_OFFSET);
        info.power = buffer.getInt(power_OFFSET);
        info.calibrated_power = buffer.getInt(calibrated_power_OFFSET);
        info.rssi = buffer.getInt(rssi_OFFSET);
        info.time = buffer.getLong(time_OFFSET);
    }

    /**
     * Callback from native code to indicate that theNativeBuffer has been updated with new event data. This happens
     * from the thread that runs the scanner loop and has attached itself to this JavaVM instance. This will dispatch
     * to the rawEventCallback, eventCallback in that preferred order.
     */
    public static boolean eventNotification() {
        boolean stop = false;
        eventCount ++;
        if(rawEventCallback != null) {
            try {
                ByteBuffer readOnly = theNativeBuffer.asReadOnlyBuffer();
                stop = rawEventCallback.beaconEvent(readOnly);
                return stop;
            } catch (Throwable e) {
                System.err.printf("Error during dispatch to rawEventCallback");
                e.printStackTrace(System.err);
            }
        }

        // Read the native buffer via theNativeBuffer
        try {
            byte uuid[] = new byte[36];
            int uuidLength = 0;
            for (int n = 0; n < uuid.length; n++) {
                byte bn = theNativeBuffer.get(n);
                if(bn == 0)
                    break;
                uuid[n] = bn;
                uuidLength ++;
            }
            String uuidStr = new String(uuid, 0, uuidLength);
            boolean isHeartbeat = theNativeBuffer.getInt(IS_HEARTBEAT_OFFSET) != 0;
            int count = theNativeBuffer.getInt(count_OFFSET);
            int code = theNativeBuffer.getInt(code_OFFSET);
            int manufacturer = theNativeBuffer.getInt(manufacturer_OFFSET);
            int major = theNativeBuffer.getInt(major_OFFSET);
            int minor = theNativeBuffer.getInt(minor_OFFSET);
            int power = theNativeBuffer.getInt(power_OFFSET);
            int calibrated_power = theNativeBuffer.getInt(calibrated_power_OFFSET);
            int rssi = theNativeBuffer.getInt(rssi_OFFSET);
            long time = theNativeBuffer.getLong(time_OFFSET);
            if(eventCallback != null) {
                Beacon beacon = new Beacon(scannerID, uuidStr, code, manufacturer, major, minor, power, rssi, time);
                beacon.setHeartbeat(isHeartbeat);
                beacon.setCount(count);
                stop = eventCallback.beaconEvent(beacon);
            } else {
                System.out.printf("event: %s,%d,%d rssi=%d, time=%d\n", uuidStr, major, minor, rssi, time);
                ByteBuffer readOnly = theNativeBuffer.asReadOnlyBuffer();
                BeaconInfo info = new BeaconInfo(readOnly);
                System.out.printf("%s\n", info);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return stop;
    }

    /**
     * Simple main entry point to validate the receipt of the beacon info messages from the bluez stack. This
     * sets the java.library.path to /usr/local/lib so that libscannerJni.so must be installed to that location.
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        int device = 0;
        if(args.length > 0)
            device = Integer.parseInt(args[0]);
        try {
            // Load
            System.setProperty("java.library.path", "/usr/local/lib");
            System.loadLibrary("scannerJni");

            ByteBuffer bb = ByteBuffer.allocateDirect(beacon_info_SIZEOF);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            HCIDump.theNativeBuffer = bb;
            HCIDump.allocScanner(bb, device);
            eventCount = 1;
            boolean running = true;
            while (running) {
                Thread.sleep(10);
                if(eventCount % 1000 == 0)
                    System.out.printf("event count=%d\n", eventCount);
            }
            HCIDump.freeScanner();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
