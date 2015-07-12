package org.jboss.summit2015.beacon.bluez;

import java.nio.ByteBuffer;

/**
 * Simple wrapper around the ByteBuffer of the beacon event that provides getters similar to the beacon_info struct
 * in C
 */
public class BeaconInfo {
    public String uuid;
    public boolean isHeartbeat;
    public int count;
    public int code;
    public int manufacturer;
    public int major;
    public int minor;
    public int power;
    public int calibrated_power;
    public int rssi;
    public long time;
    public int scannerSequenceNo;

    public BeaconInfo(byte[] rawBuffer) {
        freeze(ByteBuffer.wrap(rawBuffer));
    }
    public BeaconInfo(ByteBuffer buffer) {
        freeze(buffer);
    }

    /**
     * Read the underlying buffer into the public fields
     */
    public void freeze(ByteBuffer buffer) {
        HCIDump.freezeBeaconInfo(this, buffer);
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isHeartbeat() {
        return isHeartbeat;
    }

    public int getCount() {
        return count;
    }

    public int getCode() {
        return code;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPower() {
        return power;
    }

    public int getCalibrated_power() {
        return calibrated_power;
    }

    public int getRssi() {
        return rssi;
    }

    public long getTime() {
        return time;
    }

    public void setScannerSequenceNo(int scannerSequenceNo) {
        this.scannerSequenceNo = scannerSequenceNo;
    }

    public int getScannerSequenceNo() {
        return scannerSequenceNo;
    }

    @Override
    public String toString() {
        return "BeaconInfo{" +
            "uuid='" + uuid + '\'' +
            ", isHeartbeat=" + isHeartbeat +
            ", count=" + count +
            ", code=" + code +
            ", manufacturer=" + manufacturer +
            ", major=" + major +
            ", minor=" + minor +
            ", power=" + power +
            ", calibrated_power=" + calibrated_power +
            ", rssi=" + rssi +
            ", time=" + time +
            ", scannerSequenceNo=" + scannerSequenceNo +
            '}';
    }
}
