import org.apache.activemq.ActiveMQConnectionFactory;
import org.jboss.summit2015.beacon.Beacon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestJmsBridge implements ExceptionListener {
   static Connection connection;
   static Session session;
   @BeforeClass
   public static void init() throws Exception {
      // Create a Connection
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
   }
   @AfterClass
   public static void destroy() throws Exception {
      session.close();
      connection.stop();
   }

   @Test
   public void testReceiveTopic() throws Exception {
      connection.setExceptionListener(this);

      Destination topic  = session.createTopic("beaconEvents");
      consumeDestination(topic);
   }

   @Test
   public void testReceiveQueue() throws Exception {
      Destination queue  = session.createQueue("beaconEvents");
      consumeDestination(queue);
   }

   @Test
   public void testBrowseQueueForHeartbeats() throws Exception {
      Queue queue  = session.createQueue("beaconEvents");
      QueueBrowser browser = session.createBrowser(queue, "messageType = 1");
      Enumeration msgs = browser.getEnumeration();
      HashMap<String, Integer> heartbeatCounts = new HashMap<>();
      while(msgs.hasMoreElements()) {
         Message msg = (Message) msgs.nextElement();
         String scannerID = msg.getStringProperty("scannerID");
         Integer count = heartbeatCounts.get(scannerID);
         if(count == null) {
            count = 0;
         }
         heartbeatCounts.put(scannerID, count + 1);
      }
      System.out.printf("%s\n", heartbeatCounts);
   }

   @Test
   public void testBrowseStatusQueue() throws Exception {
      Queue queue  = session.createQueue("scannerHealth");
      QueueBrowser browser = session.createBrowser(queue);
      Enumeration msgs = browser.getEnumeration();
      while(msgs.hasMoreElements()) {
         Message msg = (Message) msgs.nextElement();
         Enumeration names = msg.getPropertyNames();
         System.out.printf("Status: %s\n", msg.getJMSMessageID());
         while(names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = msg.getStringProperty(name);
            System.out.printf("\t%s: %s\n", name, value);
         }
      }
   }

   public void onException(JMSException ex) {
      ex.printStackTrace();
   }

   private void consumeDestination(Destination destination) throws Exception {
      MessageConsumer consumer = session.createConsumer(destination);
      Message msg = consumer.receive(5000);
      System.out.printf("Received msg: %s\n", msg);
      if(msg != null) {
         // See if the message has a scannerID property
         if(msg.getStringProperty("scannerID") != null ) {
            Beacon beacon = new Beacon();
            beacon.setScannerID(msg.getStringProperty("scannerID"));
            beacon.setUUID(msg.getStringProperty("uuid"));
            beacon.setCode(msg.getIntProperty("code"));
            beacon.setManufacturer(msg.getIntProperty("manufacturer"));
            beacon.setMajor(msg.getIntProperty("major"));
            beacon.setMinor(msg.getIntProperty("minor"));
            beacon.setPower(msg.getIntProperty("power"));
            beacon.setRssi(msg.getIntProperty("rssi"));
            beacon.setTime(msg.getLongProperty("time"));
            System.out.printf("Read beacon from properties: %s\n", beacon);
         } else if(msg instanceof BytesMessage) {
            BytesMessage bmsg = BytesMessage.class.cast(msg);
            int length = (int) bmsg.getBodyLength();
            byte data[] = new byte[length];
            bmsg.readBytes(data);
            Beacon beacon = Beacon.fromByteMsg(data);
            System.out.printf("Read beacon: %s\n", beacon);
         } else {
            System.out.printf("Unexpected msg type: %s\n", msg.getClass());
         }
      }

      consumer.close();

   }
}
