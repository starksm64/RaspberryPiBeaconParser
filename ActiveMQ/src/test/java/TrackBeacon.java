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
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TrackBeacon {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";
   private static int trackID = 88;

   static void trackProcessedOnDestination(Session session, Destination destination) throws Exception  {
      final MessageConsumer consumer = session.createConsumer(destination, "user_id = "+trackID);
      Runnable runnable = () -> {
         try {
         Message msg = consumer.receive();
         while(msg != null) {
            String timestamp_s = msg.getStringProperty("timestamp_s");
            String locationID = msg.getStringProperty("location_id");
            Double locationDist = -1.0;
            if (msg.propertyExists("location_distance")) {
               Number distance = (Number) msg.getObjectProperty("location_distance");
               locationDist = distance.doubleValue();
            }
            System.out.printf("===(%d): at:%s/%.4f on: %s\n", trackID, locationID, locationDist, timestamp_s);
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
      MessageConsumer consumer = session.createConsumer(destination, "minor = "+trackID);
      Message msg = consumer.receive();
      while(msg != null) {
         int minorID = msg.getIntProperty("minor");
         if(minorID == trackID) {
            Beacon beacon = Utils.extractBeacon(msg);
            //System.out.printf("%s\n", beacon.toJSONSimple());
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
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to broker\n");
      Destination destination = session.createTopic(destinationName);
      Destination destinationProcessed = session.createTopic(destinationName+"_processed");
      trackProcessedOnDestination(session, destinationProcessed);
      trackBeaconOnDestination(session, destination);
   }
}
