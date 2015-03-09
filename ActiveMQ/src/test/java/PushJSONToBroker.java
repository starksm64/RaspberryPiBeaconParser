import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.jboss.summit2015.beacon.Beacon;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * Simple program to transform the json data from a sensor collection run to the activemq broker.
 * The messages are pushed as empty TextMessages with properties corresponding to the beacon
 * information.
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class PushJSONToBroker {

   static void populateMessage(Message message, Beacon beacon) throws JMSException {
      message.setStringProperty("uuid", beacon.getUUID());
      message.setStringProperty("scannerID", beacon.getScannerID());
      message.setIntProperty("major", beacon.getMajor());
      message.setIntProperty("minor", beacon.getMinor());
      message.setIntProperty("manufacturer", beacon.getManufacturer());
      message.setIntProperty("code", beacon.getCode());
      message.setIntProperty("power", beacon.getCalibratedPower());
      message.setIntProperty("rssi", beacon.getRssi());
      message.setLongProperty("time", beacon.getTime());
      message.setIntProperty("messageType", beacon.getMessageType());
   }

   public static void main(String[] args) throws Exception {
      System.out.printf("Parsing args: %s\n", Arrays.asList(args));

      String destinationName = "beaconEvents";
      String dataSet = "FourScannersRun%231-2015-03-07.json.gz";
      int limitCount = Integer.MAX_VALUE;
      for(int n = 0; n < args.length; n += 2) {
         switch (args[n]) {
            case "-destination":
               destinationName = args[n+1];
               break;
            case "-dataSet":
               dataSet = args[n+1];
               break;
            case "-limitCount":
               limitCount = Integer.parseInt(args[n+1]);
               break;
         }
      }

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

      // Create the message publisher sending to destinationName
      Destination destination = session.createQueue(destinationName);
      MessageProducer producer = session.createProducer(destination);

      // Get the dataSet input stream directly from git
      URL gitURL = new URL("https://github.com/starksm64/RaspberryPiBeaconParser/blob/master/data/"+dataSet+"?raw=true");
      InputStream is = gitURL.openStream();
      GZIPInputStream gzip = new GZIPInputStream(is);
      InputStreamReader reader = new InputStreamReader(gzip);

      long start = System.currentTimeMillis();
      Gson gson = new Gson();
      JsonStreamParser jsp = new JsonStreamParser(reader);
      int count = 0;
      while (jsp.hasNext()) {
         JsonElement jse = jsp.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         //System.out.printf("%d: %s\n", count++, beacon);
         TextMessage message = session.createTextMessage();
         populateMessage(message, beacon);
         producer.send(message);
         count ++;
         if(count >= limitCount)
            break;
      }
      reader.close();

      long end = System.currentTimeMillis();
      System.out.printf("Sent %d messages to %s in %d ms\n", count, destinationName, (end - start));

      session.close();
      connection.close();
   }
}
