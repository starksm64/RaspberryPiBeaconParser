import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class LoopbackTest {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";

   static void pushEvents(Session session, Topic topic) {
      int count = 0;
      try {
         MessageProducer producer = session.createProducer(topic);
         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         long start = System.currentTimeMillis();
         while (true) {
            count++;
            Message msg = session.createTextMessage();
            msg.setStringProperty("scannerID", "LoopbackTest");
            producer.send(msg);
            if ((count % 1000) == 0) {
               session.commit();
               if(count % 10000 == 0) {
                  long end = System.currentTimeMillis();
                  long rate = 1000 * count / (end - start);
                  System.out.printf("SEND: %d, rate=%d\n", count, rate);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   static void drainEvents(Session session, Topic topic) throws JMSException {
      MessageConsumer consumer = session.createConsumer(topic);
      int count = 0;
      Message msg = consumer.receive(5000);
      long start = System.currentTimeMillis();
      while(msg != null) {
         count ++;
         msg = consumer.receive(5000);
         if((count % 1000) == 0) {
            session.commit();
            long end = System.currentTimeMillis();
            long rate = 1000*count / (end - start);
            System.out.printf("RECV: %d, rate=%d\n", count, rate);
         }
      }
      System.out.printf("Scanned count: %d\n", count);
      consumer.close();
   }

   public static void main(String[] args) throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      Context context = new InitialContext(props);

      // Create a Connection
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      System.out.printf("ConnectionFactory created connection: %s\n", connection);
      connection.setExceptionListener(new ExceptionListener() {
         @Override
         public void onException(JMSException ex) {
            ex.printStackTrace();
         }
      });
      connection.start();

      Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
      System.out.printf("Created session: %s\n", session);
      Topic beaconEvents  = session.createTopic("beaconEvents");
      if(args[0].equalsIgnoreCase("send")) {
         pushEvents(session, beaconEvents);
      } else {
         drainEvents(session, beaconEvents);
      }
      session.close();
      connection.close();
   }
}
