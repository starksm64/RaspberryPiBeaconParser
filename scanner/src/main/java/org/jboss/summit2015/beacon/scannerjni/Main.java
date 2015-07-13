package org.jboss.summit2015.beacon.scannerjni;

import com.beust.jcommander.JCommander;
import org.jboss.logging.Logger;
import org.jboss.summit2015.beacon.bluez.HCIDump;
import org.jboss.summit2015.beacon.common.ParseCommand;

import java.net.SocketException;

/**
 * The main entry point for the java scanner that integrates with the JNI bluez stack code
 * @see org.jboss.summit2015.beacon.bluez.HCIDump
 */
public class Main {
    private static Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        System.loadLibrary("scannerJni");
        log.info("Loaded native scannerJni library");

        ParseCommand cmdArgs = new ParseCommand();
        JCommander cmdArgParser = new JCommander(cmdArgs);
        cmdArgParser.parse(args);

        // If scannerID is the string {IP}, replace it with the host IP address
        String scannerID = cmdArgs.scannerID;
        if(scannerID.compareTo("{IP}") == 0) {
            char hostIPAddress[] = new char[128];
            char macaddr[] = new char[32];
            try {
                HealthStatus.getHostInfo(hostIPAddress, macaddr);
                cmdArgs.scannerID = new String(hostIPAddress);
            } catch (SocketException e) {
                log.warn("Failed to read host address info", e);
            }
        }

        HCIDumpParser parser = new HCIDumpParser(cmdArgs);

        try {
            // Start the scanner parser handler threads other than the native stack handler
            parser.start();
            // Setup the native bluetooth stack integration, callbacks, and stack thread
            HCIDump.setRawEventCallback(parser::beaconEvent);
            HCIDump.initScanner(cmdArgs.hciDev);
            // Wait for an external stop notification via the marker file
            parser.waitForStop();
            // Shutdown the parser
            parser.stop();
        } catch (Exception e) {
            log.error("Scanner exiting on exception", e);
        }
        log.infof("End scanning");
        System.exit(0);
    }
}
