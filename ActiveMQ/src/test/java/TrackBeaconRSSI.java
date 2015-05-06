import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TrackBeaconRSSI {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";

   static void trackBeaconOnDestination(Session session, Destination destination) throws Exception  {
      MessageConsumer consumer = session.createConsumer(destination);
      Message msg = consumer.receive();
      while(msg != null) {
         int minorID = msg.getIntProperty("minor");
         int rssi = msg.getIntProperty("rssi");
         String uuid = msg.getStringProperty("uuid");
         System.out.printf("%d, %d, %s\n", minorID, rssi, uuid);
         msg = consumer.receive();
      }
   }
   public static void main(String[] args) throws Exception {
      String destinationName = "rawHeartbeatEvents";
      // Local connection
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to broker\n");
      Destination destination = session.createQueue(destinationName);
      trackBeaconOnDestination(session, destination);
   }
}
