import org.apache.activemq.ActiveMQConnectionFactory;
import org.jboss.summit2015.beacon.Beacon;
import org.junit.Test;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestJmsBridge implements ExceptionListener {
   @Test
   public void testBrowseTopic() throws Exception {
      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
      // Create a Connection
      Connection connection = connectionFactory.createConnection();
      connection.start();
      connection.setExceptionListener(this);

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Destination topic  = session.createTopic("beaconEvents");
      MessageConsumer consumer = session.createConsumer(topic);
      Message msg = consumer.receive(5000);
      System.out.printf("Received msg: %s\n", msg);
      if(msg != null) {
         if(msg instanceof BytesMessage) {
            BytesMessage bmsg = BytesMessage.class.cast(msg);
            int length = (int) bmsg.getBodyLength();
            byte data[] = new byte[length];
            bmsg.readBytes(data);
            Beacon beacon = Beacon.fromByteMsg(data);
            System.out.printf("Read beacon: %s\n", beacon);
         } else {
            System.out.printf("Unexpected msg type: %s\n", msg.getClass());
         }
      }

      consumer.close();
      session.close();
      connection.close();
   }
   public void onException(JMSException ex) {
      ex.printStackTrace();
   }
}
