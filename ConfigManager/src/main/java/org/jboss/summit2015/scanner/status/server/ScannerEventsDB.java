package org.jboss.summit2015.scanner.status.server;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.DuplicateDataException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.je.StatsConfig;
import org.jboss.summit2015.beacon.Beacon;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ScannerEventsDB implements MessageStore {
   private volatile static long msgCount = 0;
   private Environment myEnv;

   // The databases that our application uses
   private Database scannerHealthDB;
   private Database beaconEventsDB;
   private SecondaryDatabase beaconMinorIndexDB;
   private Sequence scannerHealthSequence;
   private BeaconBinding beaconBinding = new BeaconBinding();
   private BeaconEventKeyBinding keyBinding = new BeaconEventKeyBinding();
   private ScannerHealthBinding healthBinding = new ScannerHealthBinding();
   private StatusMonitorReporter statusReporter;

   // The setup() method opens all our databases and the environment
   // for us.
   public void setup(File envHome, boolean readOnly)
      throws DatabaseException, IOException {

      EnvironmentConfig myEnvConfig = new EnvironmentConfig();
      DatabaseConfig myDbConfig = new DatabaseConfig();
      SecondaryConfig mySecConfig = new SecondaryConfig();

      // If the environment is read-only, then
      // make the databases read-only too.
      myEnvConfig.setReadOnly(readOnly);
      myDbConfig.setReadOnly(readOnly);
      mySecConfig.setReadOnly(readOnly);

      // If the environment is opened for write, then we want to be
      // able to create the environment and databases if
      // they do not exist.
      myEnvConfig.setAllowCreate(!readOnly);
      myDbConfig.setAllowCreate(!readOnly);
      mySecConfig.setAllowCreate(!readOnly);

      // Allow transactions if we are writing to the database
      myEnvConfig.setTransactional(!readOnly);
      myDbConfig.setTransactional(!readOnly);
      mySecConfig.setTransactional(!readOnly);

      // Open the environment
      myEnv = new Environment(envHome, myEnvConfig);

      // Now open, or create and open, our databases
      // Open the vendors and inventory databases
      scannerHealthDB = myEnv.openDatabase(null, "ScannerHealthDB", myDbConfig);
      SequenceConfig config = new SequenceConfig();
      config.setAllowCreate(true);
      DatabaseEntry key = new DatabaseEntry("ScannerHealth".getBytes("UTF-8"));
      scannerHealthSequence = scannerHealthDB.openSequence(null, key, config);

      beaconEventsDB = myEnv.openDatabase(null, "BeaconEventsDB", myDbConfig);

      // Need a tuple binding for the Inventory class.
      // We use the InventoryBinding class
      // that we implemented for this purpose.
      TupleBinding inventoryBinding = new BeaconBinding();

      // Open the secondary database. We use this to create a
      // secondary index for the inventory database

      // We want to maintain an index for the inventory entries based
      // on the item name. So, instantiate the appropriate key creator
      // and open a secondary database.
      MinorKeyCreator keyCreator = new MinorKeyCreator(inventoryBinding);

      // Set up additional secondary properties
      // Need to allow duplicates for our secondary database
      mySecConfig.setSortedDuplicates(true);
      mySecConfig.setAllowPopulate(true); // Allow autopopulate
      mySecConfig.setKeyCreator(keyCreator);

      // Now open it
      beaconMinorIndexDB =
         myEnv.openSecondaryDatabase(
            null,
            "minorIDIndex", // index name
            beaconEventsDB,     // the primary db that we're indexing
            mySecConfig);    // the secondary config
   }

   public StatusMonitorReporter getStatusReporter() {
      return statusReporter;
   }

   public void setStatusReporter(StatusMonitorReporter statusReporter) {
      this.statusReporter = statusReporter;
   }

   // getter methods

   // Needed for things like beginning transactions
   public Environment getEnv() {
      return myEnv;
   }

   public Database getScannerHealthDB() {
      return scannerHealthDB;
   }

   public Database getBeaconEventsDB() {
      return beaconEventsDB;
   }

   public SecondaryDatabase getMinorIDIndexDB() {
      return beaconMinorIndexDB;
   }

   public String getStatus() {
      StatsConfig config = new StatsConfig();
      config.setFast(false);
      DatabaseStats stats = beaconEventsDB.getStats(config);
      StringBuilder status = new StringBuilder();
      status.append(String.format("BeaconEvents count: %s\n", beaconEventsDB.count()));
      status.append(String.format("BeaconEvents stats: %s\n", stats));
      stats = scannerHealthDB.getStats(config);
      status.append(String.format("ScannerHealth count: %s\n", scannerHealthDB.count()));
      status.append(String.format("ScannerHealth stats: %s\n", stats));
      return status.toString();
   }
   public void showStatus() {
      System.out.printf("%s\n", getStatus());
   }

   //Close the environment
   public void close() {
      if (myEnv != null) {
         try {
            //Close the secondary before closing the primaries
            beaconMinorIndexDB.close();
            scannerHealthSequence.close();
            scannerHealthDB.close();
            beaconEventsDB.close();

            // Finally, close the environment.
            myEnv.close();
         } catch (DatabaseException dbe) {
            System.err.println("Error closing MyDbEnv: " + dbe.toString());
         }
      }
   }

   public void putBeacon(Beacon beacon) {
      DatabaseEntry theKey = new DatabaseEntry();
      DatabaseEntry theData = new DatabaseEntry();
      keyBinding.objectToEntry(beacon, theKey);

      beaconBinding.objectToEntry(beacon, theData);

      // Put it in the database. These puts are transactionally protected
      // (we're using autocommit).
      OperationStatus status = beaconEventsDB.put(null, theKey, theData);
      if(status != OperationStatus.SUCCESS) {
         throw new DuplicateDataException(String.format("Failed to insert beacon[%d,%d]", beacon.getTime(), beacon.getMinor()));
      }
   }

   public void putStatus(Properties properties) {
      long keySeq = scannerHealthSequence.get(null, 1);
      DatabaseEntry theKey = new DatabaseEntry();
      LongBinding.longToEntry(keySeq, theKey);

      DatabaseEntry theData = new DatabaseEntry();
      healthBinding.objectToEntry(properties, theData);

      // Put it in the database. These puts are transactionally protected
      // (we're using autocommit).
      OperationStatus status = scannerHealthDB.put(null, theKey, theData);
      if(status != OperationStatus.SUCCESS) {
         throw new DuplicateDataException(String.format("Failed to insert %s", properties));
      }
   }

   public Iterable<Beacon> getBeacons() {
      Cursor cursor = beaconEventsDB.openCursor(null, null);
      BeaconsIterable iterable = new BeaconsIterable(cursor);
      return iterable;
   }

   public Iterable<Properties> getScannerStatus() {
      Cursor cursor = scannerHealthDB.openCursor(null, null);
      ScannerHealthIterable iterable = new ScannerHealthIterable(cursor);
      return iterable;
   }

   void putRawBeacon(byte[] beacon) throws IOException {
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(beacon));
      int version = dis.readInt();
      int length = dis.readInt();
      byte[] tmp = new byte[length];
      dis.readFully(tmp);
      String scannerID = new String(tmp);
      length = dis.readInt();
      tmp = new byte[length];
      dis.readFully(tmp);
      String uuid = new String(tmp);
      int code = dis.readInt();
      int manufacturer = dis.readInt();
      int major = dis.readInt();
      int minor = dis.readInt();
      int power = dis.readInt();
      int calibratedPower = dis.readInt();
      int rssi = dis.readInt();
      long time = dis.readLong();
      int messageType = dis.readInt();

      DatabaseEntry theKey = new DatabaseEntry();
      TupleOutput keyOut = new TupleOutput();
      keyOut.writeLong(time);
      keyOut.writeInt(minor);
      theKey.setData(keyOut.getBufferBytes());

      DatabaseEntry theData = new DatabaseEntry();
      TupleOutput dos = new TupleOutput();
      dos.writeString(scannerID);
      dos.writeString(uuid);
      dos.writeInt(code);
      dos.writeInt(manufacturer);
      dos.writeInt(major);
      dos.writeInt(minor);
      dos.writeInt(power);
      dos.writeInt(calibratedPower);
      dos.writeInt(rssi);
      dos.writeLong(time);
      dos.writeInt(messageType);
      theData.setData(dos.getBufferBytes());

      // Put it in the database. These puts are transactionally protected
      // (we're using autocommit).
      OperationStatus status = beaconEventsDB.put(null, theKey, theData);
      if(status != OperationStatus.SUCCESS) {
         throw new DuplicateDataException(String.format("Failed to insert beacon[%d,%d]", time, minor));
      }
//      System.out.printf("Wrote beacon: [%s,%d,%d]time(%d)=%s,rssi=%d\n", uuid, major, minor, time, new Date(time), rssi);
   }

   /**
    *
    * @param msg
    */
   private Map<String,String> putProperties(byte[] msg) throws IOException {
      long keySeq = scannerHealthSequence.get(null, 1);
      DatabaseEntry theKey = new DatabaseEntry();
      LongBinding.longToEntry(keySeq, theKey);

      DatabaseEntry theData = new DatabaseEntry();
      TupleOutput dos = new TupleOutput();
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(msg));
      int keyCount = dis.readInt();
      dos.writeInt(keyCount);
      byte[] tmp = new byte[1024];
      Map<String,String> scannerStatus = new HashMap<>();
      for(int n = 0; n < keyCount; n ++) {
         int length = dis.readInt();
         dis.readFully(tmp, 0, length);
         String key = new String(tmp, 0, length);
         length = dis.readInt();
         if(length > tmp.length)
            tmp = new byte[length];
         try {
            dis.readFully(tmp, 0, length);
         } catch (Exception e) {
            System.err.printf("Failed to read key(%s,%d/%d), length=%d\nMsg: ", key, n, keyCount, length);
            dumpMsg(msg);
            throw e;
         }
         String value = new String(tmp, 0, length);
         scannerStatus.put(key, value);
         dos.writeString(key);
         dos.writeString(value);
      }
      theData.setData(dos.getBufferBytes());

      try {
         OperationStatus status = scannerHealthDB.put(null, theKey, theData);
         if (status != OperationStatus.SUCCESS) {
            throw new DuplicateDataException(String.format("Failed to insert scanner health info"));
         }
      } catch (DatabaseException e) {
         e.printStackTrace();
      }
//      System.out.printf("Wrote scannerHealth info: %d\n", keySeq);
      return scannerStatus;
   }

   @Override
   public void store(byte[] destinationID, byte[] msg) throws IOException {
      String target = new String(destinationID);
      switch (target) {
         case "beaconEvents":
            putRawBeacon(msg);
            msgCount ++;
            break;
         case "scannerHealth":
            Map<String,String> scannerStatus = putProperties(msg);
            msgCount ++;
            if(statusReporter != null) {
               if(scannerStatus.size() == 0) {
                  System.err.printf("No properties found in status msg(size=%d):\n", msg.length);
                  dumpMsg(msg);
                  throw new InvalidPropertiesFormatException("No scanner status properties");
               } else {
                  statusReporter.monitor(scannerStatus);
               }
            }
            break;
         default:
            System.err.printf("Unknown destinationID: %s\n", destinationID);
            break;
      }
      if(msgCount % 10000 == 0)
         System.out.printf("Received %d msgs\n", msgCount);
   }

   private void dumpMsg(byte[] msg) {
      for(int b = 0; b < msg.length; b ++) {
         System.err.printf("%x", msg[b]);
      }
      System.err.println();
   }
}
