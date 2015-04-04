package org.jboss.summit2015.config;

import com.google.gson.Gson;
import org.jboss.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Java program that retrieves the scanner configuration from the mqserver and writes the local
 * scanner.config file based on the download config information
 *
 * -Djava.util.logging.manager=org.jboss.logmanager.LogManager to use jboss log manager
 * -Dorg.jboss.logging.provider=log4j
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class DownloadScannerConfig {
   private static final Logger log = Logger.getLogger(DownloadScannerConfig.class);
   private String configDir;

   public static void main(String[] args) throws Exception {
      DownloadScannerConfig configDownload = new DownloadScannerConfig();
      configDownload.run(args);
   }
   public void run(String[] args) throws Exception {
      log.infof("Parsing args: %s\n", Arrays.asList(args));

      String destinationName = UploadScannerConfig.DEFAULT_CONFIG_QUEUE;
      String brokerURL = "amqp://52.10.252.216:5672";
      for (int n = 0; n < args.length; n += 2) {
         switch (args[n]) {
            case "-destination":
               destinationName = args[n + 1];
               break;
            case "-configDir":
               configDir = args[n + 1];
               break;
            case "-brokerURL":
               brokerURL = args[n + 1];
               break;
         }
      }
      if(configDir == null) {
         configDir = System.getenv("HOME");
      }
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", brokerURL);

      Context context = new InitialContext(props);

      // Create a Connection
      ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
      Connection connection = factory.createConnection("guest", "guest");
      log.debugf("ActiveMQConnectionFactory created connection: %s\n", connection);

      connection.setExceptionListener(new ExceptionListener() {
         @Override
         public void onException(JMSException ex) {
            ex.printStackTrace();
         }
      });
      connection.start();

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      log.debugf("Created session: %s\n", session);

      int exitCode = 0;
      Queue destination = session.createQueue(destinationName);
      QueueBrowser browser = session.createBrowser(destination);
      Enumeration configs = browser.getEnumeration();
      Properties scannerProperties = null;
      BaseConfig baseConfig = null;
      while(configs.hasMoreElements()) {
         Message msg = (Message) configs.nextElement();
         if(msg instanceof TextMessage) {
            TextMessage tmsg = TextMessage.class.cast(msg);
            String json = tmsg.getText();
            Gson gson = new Gson();
            baseConfig = gson.fromJson(json, BaseConfig.class);
            log.infof("baseConfig: %s\n", baseConfig);
            // Find matching scanner properties
            scannerProperties = Utils.findPropertiesByHardwareAddress(baseConfig);
            if(scannerProperties != null)
               break;
         } else {
            log.errorf("Skipping non-TextMessage: %s\n", msg);
         }
      }
      browser.close();
      session.close();
      connection.close();

      if(scannerProperties == null) {
         List<String> addresses = Utils.getAllHardwareAddress();
         log.errorf("Failed to find scanner properties from addresses: %s\n", addresses);
         exitCode = 1;
      } else {
         writeScannerConfig(baseConfig, scannerProperties);
      }
      System.exit(exitCode);
   }

   void writeScannerConfig(BaseConfig baseConfig, Properties scannerProperties) throws IOException {
      File configFile = new File(configDir, "scanner.config");
      FileWriter scannerConfig = new FileWriter(configFile);
      // Write all properties
      for(String name : scannerProperties.stringPropertyNames()) {
         scannerConfig.write(name);
         scannerConfig.write('=');
         scannerConfig.write(scannerProperties.getProperty(name));
         scannerConfig.write('\n');
      }
      // Write the base configuration properties that don't have a scannerProperties override
      if(!scannerProperties.containsKey("brokerURL") && baseConfig.getBrokerURL() != null) {
         scannerConfig.write("brokerURL=");
         scannerConfig.write(baseConfig.getBrokerURL());
         scannerConfig.write('\n');
      }
      if(!scannerProperties.containsKey("useQueues")) {
         scannerConfig.write("useQueues=");
         scannerConfig.write(""+baseConfig.isUseQueues());
         scannerConfig.write('\n');
      }
      if(!scannerProperties.containsKey("destinationName") && baseConfig.getDestinationName() != null) {
         scannerConfig.write("destinationName=");
         scannerConfig.write(""+baseConfig.getDestinationName());
         scannerConfig.write('\n');
      }

      scannerConfig.close();
      log.infof("Wrote %s\n", configFile.getAbsolutePath());
   }
}
