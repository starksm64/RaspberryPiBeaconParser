package org.jboss.summit2015.config;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * Java program to push the beacon scanner configuration to the message broker
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class UploadScannerConfig {
   public static final String DEFAULT_CONFIG_QUEUE = "beaconScannerConfig";

   public static void main(String[] args) throws Exception {
      System.out.printf("Parsing args: %s\n", Arrays.asList(args));

      String destinationName = DEFAULT_CONFIG_QUEUE;
      String configFile = "/scannersConfig.json";
      boolean purge = true;
      for(int n = 0; n < args.length; n += 2) {
         switch (args[n]) {
            case "-destination":
               destinationName = args[n+1];
               break;
            case "-configFile":
               configFile = args[n+1];
               break;
            case "-nopurge":
               purge = false;
               break;
         }
      }

      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://localhost:5672"/*"amqp://52.10.252.216:5672"*/);

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
      connection.start();

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Created session: %s\n", session);

      // Create the message publisher sending to destinationName
      Destination destination = session.createQueue(destinationName);
      MessageProducer producer = session.createProducer(destination);

      // Get the dataSet input stream directly from git
      URL configURL = UploadScannerConfig.class.getResource(configFile);
      InputStream is = configURL.openStream();
      InputStreamReader reader = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(reader);
      StringWriter jsonConfg = new StringWriter();
      String line = br.readLine();
      while(line != null) {
         jsonConfg.append(line);
         line = br.readLine();
      }
      br.close();
      jsonConfg.close();

      if(purge) {
         int purgeCount = 0;
         MessageConsumer consumer = session.createConsumer(destination);
         try {
            // Throw away the current messages
            Message msg = consumer.receive(1000);
            while(msg != null) {
               purgeCount++;
               msg = consumer.receive(1000);
            }
            System.out.printf("Purged %d existing config messages\n", purgeCount);
         } catch (Exception e) {
         }
      }

      long start = System.currentTimeMillis();
      TextMessage configMessage = session.createTextMessage(jsonConfg.toString());
      producer.send(configMessage);
      long end = System.currentTimeMillis();
      System.out.printf("Uploaded config to: %s in %d ms\n", destinationName, (end - start));

      session.close();
      connection.close();
   }
}
