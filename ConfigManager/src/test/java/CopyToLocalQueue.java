import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class CopyToLocalQueue {
   private static final String USER = "guest";
   private static final String PASSWORD = "guest";

   public static void main(String[] args) throws Exception {
      String destinationName = "beaconEvents";
      // Local connection
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
      Context context = new InitialContext(props);
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection(USER, PASSWORD);
      connection.start();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Connected to local broker\n");
      Queue outQueue = session.createQueue(destinationName);
      MessageProducer outgoing = session.createProducer(outQueue);

      Properties propsC = new Properties();
      propsC.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      propsC.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      propsC.setProperty("queue.ingressQueue", "replayEvents");
      Context contextC = new InitialContext(propsC);
      ConnectionFactory factoryC = (ConnectionFactory) contextC.lookup("myFactoryLookup");
      Connection connectionC = factoryC.createConnection(USER, PASSWORD);
      connectionC.start();
      Session sessionC = connectionC.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic inTopic = sessionC.createTopic(destinationName);
      System.out.printf("Connected to cloud broker\n");
      MessageConsumer incoming = sessionC.createConsumer(inTopic);

      Message msg = incoming.receive();
      while(msg != null) {
         outgoing.send(msg);
      }
      outgoing.close();
      sessionC.close();
      connectionC.close();

      incoming.close();
      session.close();
      connection.close();
   }
}
