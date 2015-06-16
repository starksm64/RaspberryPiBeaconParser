import org.jboss.summit2015.beacon.Beacon;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Message;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TrackBeacon {
   private static final String USER = "demo-user";
   private static final String PASSWORD = "2015-summit-user";
   private static int trackID = 11;

   static void trackProcessedOnDestination(Session session, Destination destination) throws Exception  {
      final MessageConsumer consumer = session.createConsumer(destination, "user_id = "+trackID);
      Runnable runnable = () -> {
         try {
         Message msg = consumer.receive();
         while(msg != null) {
            long timestamp = msg.getLongProperty("timestamp");
            String timestamp_s = new Date(timestamp).toString();
            String type = msg.getStringProperty("type");
            String locationID = msg.getStringProperty("location_id");
            System.out.printf("===(%d): at:%s/%s on: %s\n", trackID, type, locationID, timestamp_s);
            msg = consumer.receive();
         }
         } catch (Exception e) {
            e.printStackTrace();
            try {
               consumer.close();
            } catch (JMSException e1) {
               e1.printStackTrace();
            }
         }
      };
      Thread t = new Thread(runnable, "ProcessedEvents");
      t.start();
   }
   static void trackBeaconOnDestination(Session session, Destination destination) throws Exception  {
      MessageConsumer consumer = session.createConsumer(destination);
      Message msg = consumer.receive();
      while(msg != null) {
         int minorID = msg.getIntProperty("minor");
         if(minorID == trackID) {
            Beacon beacon = Utils.extractBeacon(msg);
            System.out.printf("%s\n", beacon.toJSONSimple());
         }
         msg = consumer.receive();
      }
   }
   static void trackBeaconOnQueue(Session session, Destination eventsQueue) throws Exception {

      QueueBrowser browser = session.createBrowser((Queue)eventsQueue);
      Enumeration<Message> msgs = browser.getEnumeration();
      while(msgs.hasMoreElements()) {
         Message msg = msgs.nextElement();
         int minorID = msg.getIntProperty("minor");
         if(minorID == trackID) {
            Beacon beacon = Utils.extractBeacon(msg);
            //System.out.printf("+++ %s\n", beacon.toJSONSimple());
         }
      }
   }

   public static void main(String[] args) throws Exception {
      String destinationName = "beaconEvents";
      // Local connection
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://184.72.167.147:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to broker\n");
      Destination destination = session.createTopic(destinationName);
      Destination destinationProcessed = session.createTopic("VirtualTopic.beaconEvents_processed");
      trackProcessedOnDestination(session, destinationProcessed);
      trackBeaconOnDestination(session, destination);
   }
}
