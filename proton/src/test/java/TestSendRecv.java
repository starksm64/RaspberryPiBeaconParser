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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

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
         while (count < 3600) {
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

      FileWriter fw = new FileWriter("/tmp/testWriteJson.json");
      int count = 1;
      while(beacon != null) {
         try {
            beacon = (Beacon) ois.readObject();
         } catch (EOFException e) {
            break;
         }
         String jsonOutput = beacon.toJSON();
         fw.write(jsonOutput);
         count ++;
      }
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
}
