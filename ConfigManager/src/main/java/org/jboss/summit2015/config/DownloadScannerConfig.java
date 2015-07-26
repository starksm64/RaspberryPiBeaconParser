package org.jboss.summit2015.config;

import org.jboss.logging.Logger;
import org.jboss.summit2015.util.Inet;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Java program that retrieves the scanner configuration from the mqserver and writes the local
 * scanner.config file based on the download config information
 * <p>
 * -Djava.util.logging.manager=org.jboss.logmanager.LogManager to use jboss log manager
 * -Dorg.jboss.logging.provider=log4j
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class DownloadScannerConfig {
    private static final Logger log = Logger.getLogger(DownloadScannerConfig.class);
    private String configDir;
    private BaseConfig baseConfig = null;
    private boolean dryRun = false;

    public static void main(String[] args) throws Exception {
        DownloadScannerConfig configDownload = new DownloadScannerConfig();
        configDownload.run(args);
    }

    public void run(String[] args) throws Exception {
        log.infof("Parsing args: %s\n", Arrays.asList(args));

        String configQueue = UploadScannerConfig.DEFAULT_CONFIG_QUEUE;
        String dynamicConfigQueue = UploadScannerConfig.DEFAULT_DYNAMIC_CONFIG_QUEUE;
        String brokerURL = UploadScannerConfig.DEFAULT_BROKER_URL;
        for (int n = 0; n < args.length; n += 2) {
            switch (args[n]) {
                case "-configQueue":
                    configQueue = args[n + 1];
                    break;
                case "-dynamicConfigQueue":
                    dynamicConfigQueue = args[n + 1];
                    break;
                case "-configDir":
                    configDir = args[n + 1];
                    break;
                case "-brokerURL":
                    brokerURL = args[n + 1];
                    break;
                case "-dryRun":
                    dryRun = true;
                    break;
            }
        }
        if (configDir == null) {
            configDir = System.getenv("HOME");
        }
        Properties props = new Properties();
        props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
        props.setProperty("connectionfactory.myFactoryLookup", brokerURL);

        Context context = new InitialContext(props);

        // Create a Connection
        ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
        Connection connection = factory.createConnection(UploadScannerConfig.USER, UploadScannerConfig.PASSWORD);
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
        // Look at the configuration queue for an existing scanner configuration
        Properties scannerProperties = getScannerConfig(configQueue, session);

        if (scannerProperties == null) {
            List<String> addresses = Inet.getAllHardwareAddress();
            log.warnf("Failed to find scanner properties from addresses: %s\n", addresses);
            // Try to query the dynamic scanner config queue for the scanner properties
            scannerProperties = getDynamicScannerConfig(dynamicConfigQueue, session);
        }
        log.infof("Received scannerProperties=%s", scannerProperties);
        if(scannerProperties != null) {
            writeScannerConfig(baseConfig, scannerProperties);
        }

        session.close();
        connection.close();
        System.exit(exitCode);
    }

    /**
     * Use the message broker as a request/response server for asking for a dynamic configuration
     * @param dynamicConfigQueue
     * @param session
     * @return
     * @throws Exception
     */
    Properties getDynamicScannerConfig(String dynamicConfigQueue, Session session) throws Exception {
        List<Inet.InterfaceConfig> ifaces = Inet.getAllAddress();
        // Request
        Queue requestQueue = session.createQueue(dynamicConfigQueue+"Requests");
        MessageProducer producer = session.createProducer(requestQueue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        BytesMessage request = session.createBytesMessage();
        request.setIntProperty("interfaceCount", ifaces.size());
        for(int n = 0; n < ifaces.size(); n ++) {
            Inet.InterfaceConfig ic = ifaces.get(n);
            String key0 = "mac" + n;
            request.setStringProperty(key0, ic.getMacaddr());
            String key1 = "addressCount" + n;
            List<InterfaceAddress> addressList = ic.getAddressList();
            request.setIntProperty(key1, ic.getAddressList().size());
            for(int i = 0; i < addressList.size(); i ++) {
                InetAddress ia = addressList.get(i).getAddress();
                String key2 = "ip"+n;
                request.setStringProperty(key2, ia.getHostAddress());
            }
        }
        producer.send(request);
        producer.close();

        // Get the reply
        Queue responseQueue = session.createQueue(dynamicConfigQueue);
        MessageConsumer consumer = session.createConsumer(responseQueue);
        Properties scannerProperties = null;
        Message msg = consumer.receive(60*1000);
        if(msg == null) {
            log.warnf("Failed to receive response in timeout window");
            return null;
        }
        log.infof("Response from dynamicConfig: %s\n", msg);
        if (msg instanceof MapMessage) {
            scannerProperties = new Properties();
            MapMessage map = MapMessage.class.cast(msg);
            if(map.itemExists("unavailable"))
                return null;

            Enumeration<String> names = map.getPropertyNames();
            while(names.hasMoreElements()) {
                String name = names.nextElement();
                String value = map.getStringProperty(name);
                scannerProperties.put(name, value);
            }
        }
        return scannerProperties;
    }

    /**
     * Search the scanners configuration json data for a scanner specific configuration that matches this scanners
     * hardware mac address. This sets the baseConfig object as a side effect.
     *
     * @param destinationName
     * @param session
     * @return
     * @throws Exception
     */
    Properties getScannerConfig(String destinationName, Session session) throws Exception{
        Queue destination = session.createQueue(destinationName);
        QueueBrowser browser = session.createBrowser(destination);
        Enumeration configs = browser.getEnumeration();
        Properties scannerProperties = null;

        while (configs.hasMoreElements()) {
            Message msg = (Message) configs.nextElement();
            if (msg instanceof TextMessage) {
                TextMessage tmsg = TextMessage.class.cast(msg);
                String json = tmsg.getText();
                baseConfig = Utils.parseConfig(json);
                log.infof("baseConfig: %s\n", baseConfig);
                // Find matching scanner properties
                scannerProperties = Utils.findPropertiesByHardwareAddress(baseConfig);
                if (scannerProperties != null)
                    break;
            } else {
                log.errorf("Skipping non-TextMessage: %s\n", msg);
            }
        }
        browser.close();
        return scannerProperties;
    }

    /**
     * Read any existing config, update the properties, and reset the hostname if the scannerID or hostname setting
     * changes.
     *
     * @param baseConfig - the configuration
     * @param scannerProperties
     * @throws IOException
     */
    void writeScannerConfig(BaseConfig baseConfig, Properties scannerProperties) throws IOException {
        File configFile = new File(configDir, "scanner.config");
        Properties existingProps = new Properties();
        if(configFile.canRead()) {
            FileReader existingReader = new FileReader(configFile);
            existingProps.load(existingReader);
            log.infof("Loaded existing %s, content=%s", configFile, existingProps);
        }
        String prevHostname = existingProps.getProperty("hostname");
        String prevScannerID = existingProps.getProperty("scannerID");

        if(!dryRun) {
            FileWriter scannerConfig = new FileWriter(configFile);
            // Write all properties provided for this scanner
            for (String name : scannerProperties.stringPropertyNames()) {
                scannerConfig.write(name);
                scannerConfig.write('=');
                scannerConfig.write(scannerProperties.getProperty(name));
                scannerConfig.write('\n');
            }

            // Write the base configuration properties that don't have a scannerProperties override
            if (!scannerProperties.containsKey("comment") && baseConfig.getComment() != null) {
                scannerConfig.write("comment=");
                scannerConfig.write(baseConfig.getComment());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("timestamp") && baseConfig.getTimestamp() != null) {
                scannerConfig.write("timestamp=");
                scannerConfig.write(baseConfig.getTimestamp());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("version") && baseConfig.getVersion() != null) {
                scannerConfig.write("version=");
                scannerConfig.write(baseConfig.getVersion());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("brokerURL") && baseConfig.getBrokerURL() != null) {
                scannerConfig.write("brokerURL=");
                scannerConfig.write(baseConfig.getBrokerURL());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("useQueues")) {
                scannerConfig.write("useQueues=");
                scannerConfig.write("" + baseConfig.isUseQueues());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("destinationName") && baseConfig.getDestinationName() != null) {
                scannerConfig.write("destinationName=");
                scannerConfig.write("" + baseConfig.getDestinationName());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("username") && baseConfig.getUsername() != null) {
                scannerConfig.write("username=");
                scannerConfig.write("" + baseConfig.getUsername());
                scannerConfig.write('\n');
            }
            if (!scannerProperties.containsKey("password") && baseConfig.getDestinationName() != null) {
                scannerConfig.write("password=");
                scannerConfig.write("" + baseConfig.getPassword());
                scannerConfig.write('\n');
            }
            scannerConfig.close();
            log.infof("Wrote %s\n", configFile.getAbsolutePath());
        } else {
            log.infof("Skipping update of %s, dryRun\n", configFile.getAbsolutePath());
        }

        // See if the hostname needs to be updated
        String hostname = scannerProperties.getProperty("hostname");
        String scannerID = scannerProperties.getProperty("scannerID");
        log.infof("Checking hostname=%s, against prevHostname=%s", hostname, prevHostname);
        boolean updateHostname = false;
        if(hostname != null) {
            //
            if(prevHostname != null && !hostname.equals(prevHostname)) {
                log.infof("Changing hostname to: %s from: %s", hostname, prevHostname);
                updateHostname = true;
            }
        } else if(scannerID != null && prevScannerID != null && !scannerID.equals(prevScannerID)) {
            hostname = scannerID.toLowerCase();
            log.infof("ScannerID changed to: %s from: %s, setting hostname: %s", scannerID, prevScannerID, hostname);
            updateHostname = true;
        }
        if(updateHostname)
            changeHostname(hostname);
        else
            log.infof("Hostname is unchanged");
    }

    private void changeHostname(String hostname) throws IOException {
        // Have to determine the system type
        File debianVersion = new File("/etc/debian_version");
        if(debianVersion.exists()) {
            log.infof("Updating%s /etc/hostname on debian", dryRun ? "(dryRun, skipping)" : "");
            if(!dryRun) {
                FileWriter etcHostname = new FileWriter("/etc/hostname");
                etcHostname.write(hostname);
                log.infof("Done");
            }
        }
    }
}
