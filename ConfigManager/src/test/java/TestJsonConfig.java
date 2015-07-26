import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.jboss.summit2015.config.BaseConfig;
import org.jboss.summit2015.config.Utils;
import org.jboss.summit2015.util.Inet;
import org.junit.Test;

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
import java.io.FileReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestJsonConfig {
   static class Event {
      private String name;
      private String source;

      private Event(String name, String source) {
         this.name = name;
         this.source = source;
      }

      @Override
      public String toString() {
         return String.format("(name=%s, source=%s)", name, source);
      }
   }

   @Test
   public void testCollection() {
      Gson gson = new Gson();
      Collection collection = new ArrayList();
      collection.add("hello");
      collection.add(5);
      collection.add(new Event("GREETINGS", "guest"));
      String json = gson.toJson(collection);
      System.out.println("Using Gson.toJson() on a raw collection: " + json);
      JsonParser parser = new JsonParser();
      JsonArray array = parser.parse(json).getAsJsonArray();

      String message = gson.fromJson(array.get(0), String.class);
      int number = gson.fromJson(array.get(1), int.class);
      Event event = gson.fromJson(array.get(2), Event.class);
      System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message, number, event);
   }
   @Test
   public void testProperties() {
      Properties props = new Properties();
      props.setProperty("brokerURL", "host1:port");
      props.setProperty("user", "theUser1");
      props.setProperty("password", "thePassword1");
      Properties props2 = new Properties();
      props2.setProperty("brokerURL", "host2:port");
      props2.setProperty("user", "theUser2");
      props2.setProperty("password", "thePassword2");

      Gson gson = new Gson();
      String json = gson.toJson(props);
      System.out.println("Using Gson.toJson() on a raw collection: " + json);

      Collection collection = new ArrayList();
      collection.add(props);
      collection.add(props2);
      System.out.println("Using Gson.toJson() on a raw collection: " + gson.toJson(collection));
   }
   @Test
   public void testBaseConfig() {
      BaseConfig config = new BaseConfig();
      config.setBrokerURL("host1:port");
      config.setUsername("user1");
      config.setPassword("password1");
      ArrayList<Properties> roomProperties = new ArrayList<>();

      Properties props = new Properties();
      props.setProperty("brokerURL", "host1:port");
      props.setProperty("user", "theUser1");
      props.setProperty("password", "thePassword1");
      Properties props2 = new Properties();
      props2.setProperty("brokerURL", "host2:port");
      props2.setProperty("user", "theUser2");
      props2.setProperty("password", "thePassword2");
      roomProperties.add(props);
      roomProperties.add(props2);
      config.setScannerProperties(roomProperties);

      Gson gson = new Gson();
      System.out.println("Using Gson.toJson() on a raw collection: " + gson.toJson(config));
   }

   @Test
   public void testReadConfig() throws Exception {

      System.out.printf("pwd=%s\n", new File(".").getAbsolutePath());
      FileReader fr = new FileReader("src/test/resources/config.json");
      Gson gson = new Gson();
      BaseConfig baseConfig = gson.fromJson(fr, BaseConfig.class);
      System.out.printf("baseConfig: %s\n", baseConfig);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z yyyy");
      System.out.printf("now=%s\n", formatter.format(ZonedDateTime.now()));
      ZonedDateTime theTS = ZonedDateTime.parse("Tue Mar 24 05:19:58 UTC 2015", formatter);

      ZonedDateTime timestamp = baseConfig.getTimestampDate();
      System.out.printf("baseConfig.timestamp %s\n", timestamp.format(formatter));
      assert theTS.equals(timestamp);

      Properties testProps = Utils.findPropertiesByHardwareAddress(baseConfig);
      System.out.printf("findPropertiesByHardwareAddress: %s\n", testProps);
   }

   @Test
   public void testGetAll() throws Exception {
      byte[] macaddr = {0x00, 0x50, 0x56, (byte) 0xc0, 0x00, 0x08};
      String key = String.format("%02x:%02x:%02x:%02x:%02x:%02x", macaddr[0], macaddr[1], macaddr[2], macaddr[3], macaddr[4], macaddr[5]);
      System.out.printf("test format: %s\n", key);
      List<Inet.InterfaceConfig> allAddress = Inet.getAllAddress();
      allAddress.forEach((ic) -> System.out.println(ic));
   }
   @Test
   public void testDownloadConfig() throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");

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

      int exitCode = 0;
      Queue destination = session.createQueue("beaconScannerConfig");
      QueueBrowser browser = session.createBrowser(destination);
      Enumeration configs = browser.getEnumeration();
      Properties scannerProperties = null;
      while(configs.hasMoreElements()) {
         Message msg = (Message) configs.nextElement();
         if(msg instanceof TextMessage) {
            TextMessage tmsg = TextMessage.class.cast(msg);
            String json = tmsg.getText();
            Gson gson = new Gson();
            BaseConfig baseConfig = gson.fromJson(json, BaseConfig.class);
            System.out.printf("baseConfig: %s\n", baseConfig);
            // Find matching scanner properties
            scannerProperties = Utils.findPropertiesByHardwareAddress(baseConfig);
            if(scannerProperties != null)
               break;
         } else {
            System.err.printf("Skipping non-TextMessage: %s\n", msg);
         }
      }
      browser.close();
      session.close();
      connection.close();
   }
}
