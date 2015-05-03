import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BrowseEventTopics {
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

   static void drainEvents(Session session, Destination queue) throws JMSException {
      MessageConsumer consumer = session.createConsumer(queue);
      consumer.setMessageListener(new MessageListener() {
         int count = 0;
         @Override
         public void onMessage(Message msg) {
            System.out.printf("First msg:\n");
            try {
               displayProperties(msg);
               count ++;
            } catch (JMSException e) {
               e.printStackTrace();
            }
         }
      });
   }

   public static void main(String[] args) throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");

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
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Created session: %s\n", session);
      System.out.printf("drainEvents\n");

      //Destination queue  = session.createQueue("ingress");
      //Queue queue  = session.createQueue("scannerHealth");
      Topic destination  = session.createTopic("beaconEvents_processed");
      drainEvents(session, destination);
      Thread.sleep(5*60*1000);
      session.close();
      connection.close();
   }
}

