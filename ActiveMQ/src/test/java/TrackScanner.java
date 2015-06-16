import org.jboss.summit2015.beacon.Beacon;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * Created by starksm on 5/21/15.
 */
public class TrackScanner {
    private static final String USER = "demo-user";
    private static final String PASSWORD = "2015-summit-user";
    private static final String scannerID = null;

    static void trackScanner(Session session, Destination destination) throws Exception  {
        MessageConsumer consumer = session.createConsumer(destination);
        Message msg = consumer.receive();
        while(msg != null) {
            String id = msg.getStringProperty("ScannerID");
            if(scannerID == null || id.equals(scannerID)) {
                Utils.dumpMessage(msg);
            }
            msg = consumer.receive();
        }
    }
    public static void main(String[] args) throws Exception {
        String destinationName = "scannerHealth";
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
        trackScanner(session, destination);
    }
}
