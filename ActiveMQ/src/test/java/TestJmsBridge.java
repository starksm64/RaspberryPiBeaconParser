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
import javax.jms.Session;

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
   public void testBrowseTopic() throws Exception {
      connection.setExceptionListener(this);

      Destination topic  = session.createTopic("beaconEvents");
      consumeDestination(topic);
   }

   @Test
   public void testBrowseQueue() throws Exception {
      Destination queue  = session.createQueue("beaconEvents");
      consumeDestination(queue);
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
