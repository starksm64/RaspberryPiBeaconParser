import org.jboss.summit2015.scanner.status.StatusProperties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestScannerStatusSummary implements Runnable {
   static volatile ConcurrentHashMap<String, ScannerInfo> scannerHeartbeats = new ConcurrentHashMap<>();
   boolean running;

   static class ScannerInfo {
      long time;
      int publishCount;

      public ScannerInfo(long time, int publishCount) {
         this.time = time;
         this.publishCount = publishCount;
      }
      public String toString(long now) {
         return String.format("%d,%d", (now-time)/1000, publishCount);
      }
   }

   public void run() {
      running = true;
      while(running) {
         long now = System.currentTimeMillis();
         ArrayList<String> summary = new ArrayList<>();
         scannerHeartbeats.forEach((key, value) -> {
            long diff = now - value.time;
            if (diff > 60000)
               System.err.printf("No heartbeat from %s for %d seconds\n", key, diff / 1000);
            summary.add(String.format("%s: %s", key, value.toString(now)));
         });
         System.out.printf("{%d}:%s; %s\n", summary.size(), new Date(now), summary);
         try {
            Thread.sleep(15000);
         } catch (InterruptedException e) {
         }
      }
   }

   public void monitorQueue(Session session) throws JMSException {
      Destination destination = session.createQueue("scannerHealth");
      MessageConsumer monitor = session.createConsumer(destination);
      monitorRun(monitor);
   }
   public void monitorTopic(Session session) throws JMSException {
      Topic destination = session.createTopic("scannerHealth");
      TopicSubscriber monitor = session.createDurableSubscriber(destination, "scannerHealthMonitor");
      monitorRun(monitor);
   }
   private void monitorRun(MessageConsumer monitor) throws JMSException {
      Message msg = monitor.receive();
      int count = 0;
      while(msg != null) {
         String scannerID = msg.getStringProperty(StatusProperties.ScannerID.name());
         long now = System.currentTimeMillis();
         if(scannerID != null) {
            String property = msg.getStringProperty(StatusProperties.PublishEventCount.name());
            int publishCount = -1;
            if(property != null)
               publishCount = Integer.parseInt(property);
            ScannerInfo info = new ScannerInfo(now, publishCount);
            scannerHeartbeats.put(scannerID, info);
            count ++;
            if(count % 50 == 0)
               System.out.printf("%s: %d\n", new Date(now), count);
         }
         msg = monitor.receive();
      }
      monitor.close();

   }

   public void monitor() throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");

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

      // Monitor thread
      Thread reporter = new Thread(this, "StatusReporter");
      reporter.start();

      monitorTopic(session);
      session.close();
      connection.close();
   }

   public static void main(String[] args) throws Exception {
      TestScannerStatusSummary scannerStatusSummary = new TestScannerStatusSummary();
      scannerStatusSummary.monitor();
   }
}

