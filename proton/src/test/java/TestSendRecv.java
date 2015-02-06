import org.apache.qpid.proton.ProtonFactory.ImplementationType;
import org.apache.qpid.proton.ProtonFactoryLoader;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.MessageFactory;
import org.apache.qpid.proton.messenger.Messenger;
import org.apache.qpid.proton.messenger.MessengerFactory;
import org.jboss.summit2015.beacon.Beacon;
import org.junit.Test;

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
      //msg.setAddress("amqp://192.168.1.107:5672/topic://beaconEvents");
      messenger.subscribe("amqp://192.168.1.107:5672/beaconEvents");
      messenger.recv();
      Message msg = messenger.get();
      Data data = (Data) msg.getBody();
      byte[] bytes = data.getValue().getArray();
      Beacon beacon = Beacon.fromByteMsg(bytes);
      System.out.printf("testRecvBytes, %s\n", beacon);

      messenger.stop();
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
