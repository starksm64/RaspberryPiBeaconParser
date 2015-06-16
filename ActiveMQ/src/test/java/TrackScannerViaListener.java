import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * Created by starksm on 5/21/15.
 */
public class TrackScannerViaListener implements MessageListener {
    private static final String USER = "demo-user";
    private static final String PASSWORD = "2015-summit-user";
    private static final String scannerID = "Lounge";

    @Override
    public void onMessage(Message msg) {
        try {
            String id = msg.getStringProperty("ScannerID");
            System.out.printf("Checking: %s\n", id);
            if(id.equals(scannerID)) {
                Utils.dumpMessage(msg);
            }
        } catch (JMSException e) {
            e.printStackTrace();
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
        TrackScannerViaListener listener = new TrackScannerViaListener();
        session.setMessageListener(listener);
        System.out.printf("Connected to broker\n");
    }
}
