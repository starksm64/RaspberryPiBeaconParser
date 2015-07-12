package org.jboss.summit2015.beacon.common;

/**
 * Simple base class for BeaconMapper implementations
 */
public class AbstractBeaconMapper implements IBeaconMapper {

    /**
     * Refresh the id to user mapping. This does nothing.
     */
    public void refresh(){}
    /**
     * Default implementation returns user named Unknown
     * @param minorID
     * @return Unknown
     */
    @Override
    public String lookupUser(int minorID) {
        return "Unknown";
    }
}
