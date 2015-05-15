import org.jboss.summit2015.beacon.Beacon;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class RecordBeaconEventstoJSON {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";
   private static final File STOP = new File("/Users/starksm/tmp/STOP");

   static void dumpTopicToJSON(Session session, Destination destination) throws Exception {

      MessageConsumer consumer = session.createConsumer(destination);
      int count = 0;
      FileWriter fw = new FileWriter("/Users/starksm/tmp/dumpTopicToJSON.json", true);
      Beacon beacon = null;
      Message msg = consumer.receive();
      while(msg != null) {
         beacon = Utils.extractBeacon(msg);
         String jsonOutput = beacon.toJSONSimple();
         fw.write(jsonOutput);
         fw.write('\n');
         count ++;
         msg = consumer.receive(10000);
         if(STOP.exists())
            break;
         if(count % 10000 == 0)
            System.out.printf("%d: %s\n", count, jsonOutput);
      }
      System.out.printf("Last beacon: %s\n", beacon);
      System.out.printf("Converted %d beacons\n", count++);
      fw.close();
   }

   public static void main(String[] args) throws Exception {
      if(STOP.exists())
         STOP.delete();
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
      dumpTopicToJSON(session, destination);
      session.close();
      connection.close();
   }
}
