import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Program to display the unique scanners publishing events.
 */
public class DisplayUniqueScanners {
    private static final String USER = "demo-user";
    private static final String PASSWORD = "2015-summit-user";
    private static final String scannerID = null;
    private static Map<String, Integer> scannersCount = new ConcurrentHashMap<>();

    static void resportScannerStats() {
        System.out.printf("+++ resportScannerStats(%d): %s\n", scannersCount.size(), new Date());
        scannersCount.forEach((id, count) -> System.out.printf("\t%s: %d\n", id, count));
        System.out.printf("---\n");
    }
    static void trackScanner(Session session, Destination destination) throws Exception  {
        MessageConsumer consumer = session.createConsumer(destination);
        Message msg = consumer.receive();
        while(msg != null) {
            String id = msg.getStringProperty("scannerID");
            if(scannerID == null || id.equals(scannerID)) {
                Integer count = scannersCount.get(id);
                if(count == null)
                    count = 0;
                count = count + 1;
                scannersCount.put(id, count);
            }
            msg = consumer.receive();
        }
    }
    public static void main(String[] args) throws Exception {
        String destinationName = " beaconEvents";
        // Local connection
        Properties props = new Properties();
        props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
        //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
        props.setProperty("connectionfactory.myFactoryLookup", "amqp://184.72.167.147:5672");
        Context context = new InitialContext(props);
        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        Connection connection = factory.createConnection(USER, PASSWORD);
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        System.out.printf("Connected to broker\n");
        Destination destination = session.createTopic(destinationName);
        ScheduledExecutorService timerExec = Executors.newScheduledThreadPool(1);
        timerExec.scheduleWithFixedDelay(DisplayUniqueScanners::resportScannerStats, 0, 30, TimeUnit.SECONDS);
        trackScanner(session, destination);
    }
}
