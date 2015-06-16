import org.jboss.summit2015.scanner.status.StatusProperties;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestScannerStatus {
   @Test
   public void testReadScannerHealth() throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");

      Context context = new InitialContext(props);

      // Create a Connection
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection("guest", "guest");
      System.out.printf("ActiveMQConnectionFactory created connection: %s\n", connection);

      connection.setExceptionListener(new ExceptionListener() {
         @Override
         public void onException(JMSException ex) {
            ex.printStackTrace();
         }
      });
      connection.setClientID("scannerHealthMonitor");
      connection.start();

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Created session: %s\n", session);

      int count = 0;
      Topic destination = session.createTopic("scannerHealth");
      TopicSubscriber monitor = session.createDurableSubscriber(destination, "scannerHealthMonitor");
      Message msg = monitor.receive();
      HashMap<String,String> beaconCounts = new HashMap<>();
      while(msg != null) {
         /*
         System.out.printf("Msg(%d)\n", msg.getJMSTimestamp());
         for (StatusProperties sp : StatusProperties.values()) {
            String value = msg.getStringProperty(sp.name());
            System.out.printf("\t%s=%s\n", sp, value);
         }
         */
         String eventsWindow = msg.getStringProperty(StatusProperties.EventsWindow.name());
         String[] beacons = eventsWindow.split("; ");
         for(String beacon : beacons) {
            String[] pair = beacon.split("=");
            beaconCounts.put(pair[0], pair[1]);
         }
         if(count % 10 == 0) {
            System.out.printf("Unique beacons(%d): %s\n", beaconCounts.size(), beaconCounts.keySet());
         }
         msg = monitor.receive();
      }
      monitor.close();
      session.close();
      connection.close();

   }

    @Test
    public void testIsReachable() throws Exception {
        InetAddress addr = InetAddress.getByName("192.168.1.1");
        boolean reachable = addr.isReachable(5000);
        System.out.printf("%s is reachable: %s\n", addr, reachable);
    }
}
