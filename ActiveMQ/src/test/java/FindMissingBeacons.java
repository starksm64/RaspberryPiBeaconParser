import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

/**
 * Identify which beacons have not been reported by a scanner for N seconds(default=30)
 */
public class FindMissingBeacons {
    private static final String USER = "demo-user";
    private static final String PASSWORD = "2015-summit-user";
    private static ArrayList<Integer> counts = new ArrayList<>();

    static void countReporter() {
        // Report the ids with no counts every 30 seconds
        while (true) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("\n--- Beacons with no scans: %s\n", new Date());
            int printCount = 0;
            for(int n = 1; n <= 300; n ++) {
                if(counts.get(n) == 0) {
                    System.out.printf("%d ", n);
                    printCount ++;
                    if(printCount % 10 == 0)
                        System.out.println();
                }
            }
            System.out.printf("\nCount=%d\n", printCount);
        }
    }
    static void countBeaconScans(Session session, Destination destination) throws Exception  {
        MessageConsumer consumer = session.createConsumer(destination);
        Message msg = consumer.receive();
        while(msg != null) {
            int minorID = msg.getIntProperty("minor");
            if(minorID <= 300) {
                int count = counts.get(minorID);
                count ++;
                counts.set(minorID, count);
            }
            msg = consumer.receive();
        }
    }
    public static void main(String[] args) throws Exception {
        String destinationName = "beaconEvents";
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
        for(int n = 0; n <= 300; n ++)
            counts.add(0);
        Thread counter = new Thread(FindMissingBeacons::countReporter);
        counter.start();
        countBeaconScans(session, destination);
    }
}
