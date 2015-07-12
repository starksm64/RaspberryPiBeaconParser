package org.jboss.summit2015.beacon.scannerjni;

import org.jboss.summit2015.beacon.common.StatusInformation;

/**
 * Created by starksm on 7/11/15.
 */
public interface ScannerView {
    void displayStatus(StatusInformation statusInformation);

    boolean isDisplayBeaconsMode();
}
