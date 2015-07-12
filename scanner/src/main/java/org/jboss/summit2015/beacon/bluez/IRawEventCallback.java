package org.jboss.summit2015.beacon.bluez;

/**
 * A raw beacon event callback that passes in the native byte[] array on the native thread.
 */
@FunctionalInterface
public interface IRawEventCallback {
    /**
     * Callback on the native parser thread with the shared byte[] array associated with the direct
     * ByteBuffer associated with the GetDirectBufferAddress call.
     * @param beaconInfo the beacon info array, must be copied as it would be modified on the next beacon
     *                   event from the bluetooth stack.
     */
    public boolean beaconEvent(byte[] beaconInfo);
}
