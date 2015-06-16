package org.jboss.summit2015.scanner.status.javafx;

import org.jboss.summit2015.scanner.status.StatusProperties;
import org.jboss.summit2015.scanner.status.model.ScannerInfo;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Monitor the scanner health message topic and validate the liveness of the scanners
 */
public class ScannerHealthMonitor {
    private static volatile ConcurrentHashMap<String, ScannerInfo> scannerHeartbeats = new ConcurrentHashMap<>();
    private String destinationName = "scannerHealth";
    private Session session;
    private Thread consumeThread;
    private ScheduledExecutorService statusService = Executors.newScheduledThreadPool(1);
    private MainController controller;

    /**
     * Connect to the message broker and start background threads to consume the heartbeat messages and update the
     * scanner time since last heartbeat status.
     *
     * @param username
     * @param password
     * @throws Exception
     */
    public void connect(String username, String password) throws Exception {
        // Local connection
        Properties props = new Properties();
        props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");
        //props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
        props.setProperty("connectionfactory.myFactoryLookup", "amqp://184.72.167.147:5672");
        Context context = new InitialContext(props);
        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        Connection connection = factory.createConnection(username, password);
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        System.out.printf("Connected to broker\n");
        // The message consumer thread
        consumeThread = new Thread(this::consumeMessages, "ScannerHealthMonitorConsumer");
        consumeThread.setDaemon(true);
        consumeThread.start();
        // The time since last heartbeat handler
        statusService.scheduleAtFixedRate(this::updateTimeSinceHeartbeat, 0, 15, TimeUnit.SECONDS);
    }

    /**
     * Entry point for the status heartbeat message consumer thread.
     */
    private void consumeMessages() {
        try {
            Destination destination = session.createTopic(destinationName);
            MessageConsumer consumer = session.createConsumer(destination);
            Message msg = consumer.receive();
            while(msg != null) {
                ScannerInfo info = toScannerInfo(msg);
                String scannerID = info.getScannerID();
                if(scannerHeartbeats.containsKey(scannerID) == false)
                    scannerHeartbeats.put(scannerID, info);
                else {
                    ScannerInfo prevInfo = scannerHeartbeats.get(scannerID);
                    prevInfo.setLastStatus(info.getLastStatus());
                    prevInfo.setTime(info.getTime());
                    prevInfo.setPublishCount(info.getPublishCount());
                    info = prevInfo;
                }
                controller.updateScanner(info);
                // Next heartbeat message
                msg = consumer.receive();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point to check the time since the last heartbeat. Any scanners with more than 60 seconds without
     * a heartbeat are passed to the MainController::missingHeartbeats method.
     */
    private void updateTimeSinceHeartbeat() {
        // Update the time since the last heartbeat
        long now = System.currentTimeMillis();
        ArrayList<ScannerInfo> missedHeartbeat = new ArrayList<>();
        scannerHeartbeats.forEach((key, value) -> {
            long diff = now - value.getTime();
            value.setSinceLastHeartbeat(diff);
            if(diff > 60000)
                missedHeartbeat.add(value);
            if(value.getScannerID().equals("Room203"))
                System.out.printf("=== Room203, diff=%d\n", diff);
        });
        if(missedHeartbeat.size() > 0) {
            System.out.printf("+++ WARN: missing heartbeats: %s\n", missedHeartbeat);
        }
        controller.missingHeartbeats(missedHeartbeat);
    }

    private ScannerInfo toScannerInfo(Message msg) throws JMSException {
        String scannerID = msg.getStringProperty(StatusProperties.ScannerID.name());
        long time = msg.getLongProperty(StatusProperties.SystemTimeMS.name());
        int publishCount = msg.getIntProperty(StatusProperties.PublishEventCount.name());
        Map<String, String> scannerStatus = new HashMap<>();
        Enumeration<String> names = msg.getPropertyNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            String value = msg.getStringProperty(name);
            scannerStatus.put(name, value);
        }
        ScannerInfo info = new ScannerInfo(scannerID, time, publishCount, scannerStatus);
        return info;
    }

    public void setMainController(MainController controller) {
        this.controller = controller;
    }
}
