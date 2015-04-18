import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.scanner.status.server.BeaconBinding;
import org.jboss.summit2015.scanner.status.server.BeaconEventKeyBinding;
import org.jboss.summit2015.scanner.status.server.BeaconScannerServer;
import org.jboss.summit2015.scanner.status.server.ScannerEventsDB;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestBerkleyDB {
   @Test
   public void writeBeacon() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File("/tmp/ScannerEvents");
      tmp.mkdir();
      db.setup(tmp, false);

      Beacon beacon = new Beacon();
      beacon.setScannerID("Room201");
      beacon.setUUID("DAF246CE836311E4B116123B93F75CBA");
      beacon.setCode(12345);
      beacon.setManufacturer(45678);
      beacon.setMajor(0);
      beacon.setMinor(130);
      beacon.setPower(-41);
      beacon.setRssi(-42);
      beacon.setTime(1429131681622l);
      db.putBeacon(beacon);

      beacon.setScannerID("Room202");
      beacon.setMinor(131);
      beacon.setTime(1429131681623l);
      db.putBeacon(beacon);

      db.close();
   }

   @Test
   public void directReadBeacon() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File("/tmp/ScannerEvents");
      db.setup(tmp, true);

      // Get a cursor
      Cursor cursor = db.getBeaconEventsDB().openCursor(null, null);

      // DatabaseEntry objects used for reading records
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();

      TupleBinding beaconBinding = new BeaconBinding();
      BeaconEventKeyBinding keyBinding = new BeaconEventKeyBinding();

      try { // always want to make sure the cursor gets closed
         while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            Beacon beacon = (Beacon) beaconBinding.entryToObject(foundData);
            Beacon beaconKey = keyBinding.entryToObject(foundKey);
            System.out.printf("[%d,%d] = %s\n", beaconKey.getTime(), beaconKey.getMinor(), beacon);
         }
      } catch (Exception e) {
         System.err.println("Error on inventory cursor:");
         e.printStackTrace();
      } finally {
         cursor.close();
      }

   }
   @Test
   public void readBeacons() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File("/tmp/ScannerEvents");
      db.setup(tmp, true);
      Iterable<Beacon> beaconIterator = db.getBeacons();
      for(Beacon beacon : beaconIterator) {
         System.out.printf("%s\n", beacon);
      }
   }

   @Test
   public void writeStatus() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File("/tmp/ScannerEvents");
      tmp.mkdir();
      db.setup(tmp, false);

      Properties properties = new Properties();
      properties.setProperty("ScannerID", "testSocket");
      properties.setProperty("HostIPAddress", "127.0.0.1");
      properties.setProperty("SystemTime", "2015-04-15 13:57:51");
      properties.setProperty("SystemTimeMS", "1429131471594");
      properties.setProperty("Uptime", "uptime: 1193, days:0, hrs:0, min:19, sec:53");
      properties.setProperty("Procs", "1028");
      properties.setProperty("LoadAverage", "0.58, 0.87, 0.68");
      properties.setProperty("RawEventCount", "78337");
      properties.setProperty("PublishEventCount", "9544");
      properties.setProperty("HeartbeatCount", "9897");
      properties.setProperty("HeartbeatRSSI", "-43");
      properties.setProperty("EventsWindow", "+66: 253; +81: 249; +88: 250; +130: 253; +138: 260; +149: 238; +159: 258; +999: 255");
      properties.setProperty("MemTotal", "435");
      properties.setProperty("MemFree", "85");
      properties.setProperty("SwapTotal", "511");
      properties.setProperty("SwapFree", "511");
      db.putStatus(properties);

      db.close();
   }

   @Test
   public void readStatus() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File("/tmp/ScannerEvents");
      tmp.mkdir();
      db.setup(tmp, false);

      for (Properties properties : db.getScannerStatus()) {
         System.out.printf("%s\n", properties);
      }
   }

   @Test
   public void readBeaconScannerServer() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File(BeaconScannerServer.DB_ROOT);
      db.setup(tmp, true);
      Iterable<Beacon> beaconIterator = db.getBeacons();
      for(Beacon beacon : beaconIterator) {
         System.out.printf("%s\n", beacon);
      }

      Iterable<Properties> statusIterator = db.getScannerStatus();
      for(Properties props : statusIterator) {
         System.out.printf("%s\n", props);
      }
   }
   @Test
   public void readBeaconScannerServerHealthStatus() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File(BeaconScannerServer.DB_ROOT);
      db.setup(tmp, true);
      Iterable<Properties> statusIterator = db.getScannerStatus();
      for(Properties props : statusIterator) {
         System.out.printf("%s\n", props);
      }
   }
   @Test
   public void readBeaconScannerServerBeaconEvents() throws IOException {
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File(BeaconScannerServer.DB_ROOT);
      db.setup(tmp, true);
      int count = 0;
      int limit = 1000;
      int skipCount = 1237;
      Iterable<Beacon> beaconIterator = db.getBeacons();
      for(Beacon beacon : beaconIterator) {
         count ++;
         if(count % skipCount == 0)
            System.out.printf("%s\n", beacon);
      }
      System.out.printf("Total count: %d\n", count);
   }

   @Test
   public void dumpBeaconsToJSON() throws Exception {
      FileWriter fw = new FileWriter("/tmp/dumpBeaconsToJSON.json", true);
      ScannerEventsDB db = new ScannerEventsDB();
      File tmp = new File(BeaconScannerServer.DB_ROOT);
      db.setup(tmp, true);
      int count = 0;
      Iterable<Beacon> beaconIterator = db.getBeacons();
      for(Beacon beacon : beaconIterator) {
         String jsonOutput = beacon.toJSON();
         fw.write(jsonOutput);
         count ++;
      }
      fw.close();
      System.out.printf("Total count: %d\n", count);
   }

}
