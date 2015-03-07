import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.apache.qpid.proton.ProtonFactory.ImplementationType;
import org.apache.qpid.proton.ProtonFactoryLoader;
import org.apache.qpid.proton.TimeoutException;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.MessageFactory;
import org.apache.qpid.proton.messenger.Messenger;
import org.apache.qpid.proton.messenger.MessengerFactory;
import org.jboss.summit2015.beacon.Beacon;
import org.junit.Test;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestSendRecv {
   private Beacon createBeacon() {
      String scannerID = "testSerializeBeacon";
      String uuid = "15DAF246CE836311E4B116123B93F75C";
      int code = 2;
      int manufacturer = 3852;
      int major = 47616;
      int minor = 12345;
      int power = -253;
      int rssi = -62;
      long time = System.currentTimeMillis();
      // String scannerID, String uuid, int code, int manufacturer, int major, int minor, int power, int rssi) {
      Beacon beacon = new Beacon(scannerID, uuid, code, manufacturer, major, minor, power, rssi, time);
      return beacon;
   }

   @Test
   public void testSendBytes() throws Exception {
      ProtonFactoryLoader<MessengerFactory> factoryLoader = new ProtonFactoryLoader(MessengerFactory.class, ImplementationType.PROTON_J);
      ProtonFactoryLoader<MessageFactory> messageLoader = new ProtonFactoryLoader(MessageFactory.class, ImplementationType.PROTON_J);
      MessengerFactory factory = factoryLoader.loadFactory();
      MessageFactory msgFactory = messageLoader.loadFactory();
      Messenger messenger = factory.createMessenger("testSendBytes");
      messenger.start();
      Message msg = msgFactory.createMessage();
      //msg.setAddress("amqp://192.168.1.107:5672/topic://beaconEvents");
      msg.setAddress("amqp://192.168.1.107:5672/beaconEvents");
      Beacon beacon = createBeacon();
      byte[] bytes = beacon.toByteMsg();
      msg.setBody(new Data(new Binary(bytes)));
      messenger.put(msg);
      messenger.send();
      messenger.stop();
   }
   @Test
   public void testRecvBytes() throws Exception {
      ProtonFactoryLoader<MessengerFactory> factoryLoader = new ProtonFactoryLoader(MessengerFactory.class, ImplementationType.PROTON_J);
      MessengerFactory factory = factoryLoader.loadFactory();
      Messenger messenger = factory.createMessenger("testRecvBytes");
      messenger.start();
      messenger.subscribe("amqp://192.168.1.107:5672/beaconEvents");
      //messenger.subscribe("amqp://192.168.1.107:5672/topic://beaconEvents");
      messenger.recv();
      Message msg = messenger.get();
      Data data = (Data) msg.getBody();
      byte[] bytes = data.getValue().getArray();
      Beacon beacon = Beacon.fromByteMsg(bytes);
      System.out.printf("testRecvBytes, %s\n", beacon);

      messenger.stop();
   }

   /**
    * Drain the queue and serialize the beacons to a file
    * @throws Exception
    */
   @Test
   public void testDrainQueue() throws Exception {
      ProtonFactoryLoader<MessengerFactory> factoryLoader = new ProtonFactoryLoader(MessengerFactory.class, ImplementationType.PROTON_J);
      MessengerFactory factory = factoryLoader.loadFactory();
      Messenger messenger = factory.createMessenger("testDrainQueue");
      messenger.start();
      messenger.setTimeout(5000);
      messenger.subscribe("amqp://192.168.1.107:5672/beaconEvents");
      //
      FileOutputStream fos = new FileOutputStream("/tmp/beaconEvents.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      int count = 0;
      messenger.recv();
      try {
         while (count < 38942) {
            Message msg = messenger.get();
            Data data = (Data) msg.getBody();
            byte[] bytes = data.getValue().getArray();
            Beacon beacon = Beacon.fromByteMsg(bytes);
            oos.writeObject(beacon);
            count++;
            messenger.recv(1);
         }
      } catch (TimeoutException e) {
         e.printStackTrace();
      }
      oos.close();
      System.out.printf("testDrainQueue, count=%d\n", count);

      messenger.stop();
   }

   @Test
   public void testReadBeaconFile() throws Exception {
      FileInputStream fis = new FileInputStream("/tmp/beaconEvents.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      Beacon beacon = (Beacon) ois.readObject();
      int count = 0;
      while(beacon != null) {
         System.out.printf("%d: %s\n", count++, beacon);
         beacon = (Beacon) ois.readObject();
      }
      ois.close();
   }

   @Test
   public void testCovertSerFileToJson() throws Exception {
      FileInputStream fis = new FileInputStream("/tmp/beaconEvents.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      Beacon beacon = (Beacon) ois.readObject();
      System.out.printf("First beacon: %s\n", beacon);
      FileWriter fw = new FileWriter("/tmp/testWriteJson.json", true);
      int count = 1;
      while(beacon != null) {
         String jsonOutput = beacon.toJSON();
         fw.write(jsonOutput);
         try {
            beacon = (Beacon) ois.readObject();
            count ++;
         } catch (EOFException e) {
            break;
         }
      }
      System.out.printf("Last beacon: %s\n", beacon);
      System.out.printf("Converted %d beacons\n", count++);
      fw.close();
      ois.close();
   }

   @Test
   public void testReadJsonFile() throws Exception {
      FileReader fr = new FileReader("/tmp/testWriteJson.json");
      Gson gson = new Gson();
      JsonStreamParser jsp = new JsonStreamParser(fr);
      int count = 0;
      while (jsp.hasNext()) {
         JsonElement jse = jsp.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         System.out.printf("%d: %s\n", count++, beacon);
      }
      fr.close();
   }

   /**
    * Create a graph of the
    */
   @Test
   public void generateTwoScannersRun1TimeSeries() throws Exception {
      generateTwoScannersRun1TimeSeries("../data/TwoScannersRun#1-2015-03-02.json.gz");
   }
   @Test
   public void generateTwoScannersRun2TimeSeries() throws Exception {
      generateTwoScannersRun1TimeSeries("../data/TwoScannersRun#1-2015-03-03.json.gz");
   }

   @Test
   public void testSendProperties() throws Exception {
      ProtonFactoryLoader<MessengerFactory> factoryLoader = new ProtonFactoryLoader(MessengerFactory.class, ImplementationType.PROTON_J);
      ProtonFactoryLoader<MessageFactory> messageLoader = new ProtonFactoryLoader(MessageFactory.class, ImplementationType.PROTON_J);
      MessengerFactory factory = factoryLoader.loadFactory();
      MessageFactory msgFactory = messageLoader.loadFactory();
      Messenger messenger = factory.createMessenger("testSendBytes");
      messenger.start();
      Message msg = msgFactory.createMessage();
      //msg.setAddress("amqp://192.168.1.107:5672/topic://beaconEvents");
      msg.setAddress("amqp://192.168.1.107:5672/beaconEvents");
      Beacon beacon = createBeacon();
      Map<String, Object> beaconProps = beacon.toProperties();
      msg.setApplicationProperties(new ApplicationProperties(beaconProps));
      messenger.put(msg);

      messenger.send();
      messenger.stop();
   }

   @Test
   public void testRecvProperties() throws Exception {
      ProtonFactoryLoader<MessengerFactory> factoryLoader = new ProtonFactoryLoader(MessengerFactory.class, ImplementationType.PROTON_J);
      MessengerFactory factory = factoryLoader.loadFactory();
      Messenger messenger = factory.createMessenger("testSendBytes");
      messenger.start();

      messenger.subscribe("amqp://192.168.1.107:5672/beaconEvents");
      messenger.recv();
      Message msg = messenger.get();
      Map<String, Object> beaconProps = msg.getApplicationProperties().getValue();
      Beacon beacon = Beacon.fromProperties(beaconProps);
      System.out.printf("testRecvProperties, %s\n", beacon);

      messenger.stop();
   }

   static void generateTwoScannersRun1TimeSeries(String fileName) throws Exception {
      HashMap<String, ArrayList<Beacon>> scannerData = new HashMap<>();
      FileInputStream fis = new FileInputStream(fileName);
      GZIPInputStream gzip = new GZIPInputStream(fis);
      InputStreamReader isr = new InputStreamReader(gzip);
      Gson gson = new Gson();
      JsonStreamParser jsp = new JsonStreamParser(isr);
      long firstTime = Long.MAX_VALUE;
      while (jsp.hasNext()) {
         JsonElement jse = jsp.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         if(beacon.getMajor() == 14 && beacon.getMinor() == 1) {
            ArrayList<Beacon> beacon1Data = scannerData.get(beacon.getScannerID());
            if(beacon1Data == null) {
               beacon1Data = new ArrayList<>();
               scannerData.put(beacon.getScannerID(), beacon1Data);
               long time = beacon.getTime();
               firstTime = Math.min(firstTime, time);
            }
            beacon1Data.add(beacon);
         }
      }
      isr.close();

      for (ArrayList<Beacon> beacon1Data : scannerData.values()) {
         System.out.printf("Found %d beacon1 events for scanner: %s\n", beacon1Data.size(), beacon1Data.get(0).getScannerID());
         for (Beacon beacon : beacon1Data) {
            long time = beacon.getTime();
            int rssi = beacon.getRssi();
            int calibratedPower = beacon.getCalibratedPower();
            double distance = estimateDistance(calibratedPower, rssi);
            System.out.printf("%s,%d,%.0f\n", beacon.getScannerID(), (time - firstTime), distance);
         }
      }
   }

   static double estimateDistance(int calibratedPower, double rssi) {
     if (rssi == 0) {
       return -1.0; // if we cannot determine accuracy, return -1.
     }

     double ratio = rssi*1.0/calibratedPower;
     if (ratio < 1.0) {
       return Math.pow(ratio,10);
     }
     else {
       double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
       return accuracy;
     }
   }
}
