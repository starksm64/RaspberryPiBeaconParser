import org.jboss.summit2015.beacon.Beacon;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ProcessBeaconEvents {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";
   private static final String destinationName = "beaconEvents";
   private ConcurrentLinkedDeque<List<Beacon>> averagedEvents = new ConcurrentLinkedDeque<>();
   private Session session;
   private int processedCount;
   private int rawCount;
   // A beacon id to track all processing of
   private int traceID = 81;

   void pushAverageEvents() {
      try {
         Destination destination = session.createTopic(destinationName + "_processed");
         MessageProducer producer = session.createProducer(destination);
         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

         while (true) {
            if (averagedEvents.peekFirst() != null) {
               List<Beacon> bucket = averagedEvents.pop();
               bucket.forEach((beacon) -> {
                  double distance = Utils.estimateDistance(beacon.getCalibratedPower(), beacon.getRssi());
                  try {
                     int id = beacon.getMinor();
                     Message message = session.createBytesMessage();
                     message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                     message.setIntProperty("user_id", id);
                     message.setLongProperty("timestamp", beacon.getTime());
                     message.setStringProperty("location_id", beacon.getScannerID());
                     message.setStringProperty("timestamp_s", new Date(beacon.getTime()).toString());
                     message.setDoubleProperty("location_distance", distance);
                     processedCount ++;
                     if(processedCount % 100 == 0)
                        System.out.printf("Avg[%s]: %d\n", new Date(), processedCount);
                  /*,
                            "": bucket_to_s(bucket),
                            "": locations[who][0],
                            "": ctime(bucket_to_s(bucket)),
                            "": locations[who][1]}
                   print event['timestamp_s'], event['user_id'], event['location_id']
                   */
                     if(traceID == id)
                        System.out.printf("Avg[%d], location_id=%s, location_distance=%.2f\n", id, beacon.getScannerID(), distance);
                     producer.send(message);
                  } catch (JMSException e) {
                     e.printStackTrace();
                  }
               });
            } else {
               Thread.sleep(1);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.err.printf("pushAverageEvents exiting\n");
      }
   }

   void consumeRawEvents() {
      try {
         Destination destination = session.createTopic(destinationName);
         MessageConsumer consumer = session.createConsumer(destination);
         EventsWindow window5 = new EventsWindow();
         window5.setTraceID(traceID);

         Message msg = consumer.receive();
         while (msg != null) {
            Beacon beacon = Utils.extractBeacon(msg);
            List<Beacon> events = window5.addEvent(beacon);
            rawCount ++;
            if(rawCount % 1000 == 0)
               System.out.printf("Raw[%s]: %d\n", new Date(), rawCount);
            if (events != null)
               averagedEvents.push(events);
            msg = consumer.receive();
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.err.printf("consumeRawEvents exiting\n");
      }
   }

   void run() throws Exception {
      // Local connection
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      connection.start();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to broker\n");

      Thread rawEvents = new Thread(this::consumeRawEvents, "ConsumeRawEvents");
      rawEvents.start();
      Thread processedEvents = new Thread(this::pushAverageEvents, "ProcessEvents");
      processedEvents.start();
      rawEvents.join();
   }

   public static void main(String[] args) throws Exception {
      ProcessBeaconEvents pbe = new ProcessBeaconEvents();
      pbe.run();
   }
}
