import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BrowseEvents {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";

   static void displayProperties(Message msg) throws JMSException {
      long ts = msg.getJMSTimestamp();
      System.out.printf("JMSTimestamp: %s[%s]\n", ts, new Date(ts));
      Enumeration names = msg.getPropertyNames();
      while(names.hasMoreElements()) {
         String name = (String) names.nextElement();
         String value = msg.getStringProperty(name);
         if(name.equals("time")) {
            long time = Long.valueOf(value);
            System.out.printf("\t%s=%d[%s]\n", name, time, new Date(time));
         } else {
            System.out.printf("\t%s=%s\n", name, value);
         }
      }
   }
   static void browseEvents(Session session, Queue queue) throws JMSException {
      QueueBrowser browser = session.createBrowser(queue);
      Enumeration msgs = browser.getEnumeration();
      int count = 0;
      Message lastMsg = null;
      while(msgs.hasMoreElements()) {
         Message msg = (Message) msgs.nextElement();
         lastMsg = msg;
         displayProperties(msg);
         count ++;
      }
      System.out.printf("Scanned count: %d, lastMsg=%s\n", count, lastMsg);
      browser.close();

   }
   static void drainEvents(Session session, Queue queue) throws JMSException {
      MessageConsumer consumer = session.createConsumer(queue);
      int count = 0;
      Message lastMsg = null;
      Message msg = consumer.receive(5000);
      System.out.printf("First msg:\n");
      displayProperties(msg);
      while(msg != null) {
         count ++;
         lastMsg = msg;
         msg = consumer.receive(5000);
         if((count % 1000) == 0)
            session.commit();
      }
      System.out.printf("Scanned count: %d\n", count);
      System.out.printf("Last msg:\n");
      displayProperties(lastMsg);
      consumer.close();
   }

   public static void main(String[] args) throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      props.setProperty("queue.ingressQueue", "beaconEvents");

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

      //Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
      System.out.printf("Created session: %s\n", session);
      System.out.printf("drainEvents\n");

      //Destination queue  = session.createQueue("ingress");
      //Queue queue  = session.createQueue("scannerHealth");
      Queue queue  = session.createQueue("beaconEvents");
      //drainEvents(session, queue);
      browseEvents(session, queue);
      session.close();
      connection.close();
   }
}
