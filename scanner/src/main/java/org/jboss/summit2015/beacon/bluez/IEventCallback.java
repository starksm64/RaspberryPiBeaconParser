package org.jboss.summit2015.beacon.bluez;

import org.jboss.summit2015.beacon.Beacon;

/**
 * Higher level callback that unwraps the raw native beacon event into a Beacon object
 */
@FunctionalInterface
public interface IEventCallback {
    public boolean beaconEvent(Beacon beacon);
}
