package org.jboss.summit2015.beacon.bluez;

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
    public native static void allocScanner(ByteBuffer bb, int device);
    public native static void freeScanner();

    /**
     * Callback from native code to indicate that theNativeBuffer has been updated with new event data. This happens
     * from the thread that runs the scanner loop and has attached itself to this JavaVM instance.
     */
    public static void eventNotification() {
        eventCount ++;
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
            System.out.printf("event: %s,%d,%d rssi=%d, time=%d\n", uuidStr, major, minor, rssi, time);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            // Load
            System.setProperty("java.library.path", "/tmp");
            System.loadLibrary("scannerJni");

            ByteBuffer bb = ByteBuffer.allocateDirect(beacon_info_SIZEOF);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            HCIDump.theNativeBuffer = bb;
            HCIDump.allocScanner(bb, 1);
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
