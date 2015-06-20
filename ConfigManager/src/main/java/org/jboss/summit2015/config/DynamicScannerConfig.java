package org.jboss.summit2015.config;

import org.jboss.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * Look for a config server host on the subnet that will provide a scanner config
 */
public class DynamicScannerConfig {
    private static final Logger log = Logger.getLogger(DownloadScannerConfig.class);
    private String configDir;
    private BaseConfig baseConfig = null;
    private StringWriter console;
    private Session session;
    private Queue requestQueue;
    private Queue replyQueue;
    private MessageProducer producer;
    private MessageProducer keepaliveProducer;
    private volatile boolean running = true;

    public static void main(String[] args) throws Exception {
        DynamicScannerConfig dynamicConfig = new DynamicScannerConfig();
        dynamicConfig.run(args);
    }

    public StringWriter getConsole() {
        return console;
    }

    public void setConsole(StringWriter console) {
        this.console = console;
    }

    /**
     * Need to generate noop requests to keep the broker from timing out the connection
     */
    public void keepaliveRequest() {
        while(running) {
            try {
                Message keepalive = session.createBytesMessage();
                keepalive.setBooleanProperty("keepalive", true);
                keepaliveProducer.send(keepalive);
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            String[] args = {};
            run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String[] args) throws Exception {
        log("Parsing args: %s\n", Arrays.asList(args));

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
            }
        }
        if (configDir == null) {
            configDir = System.getenv("HOME");
        }
        loadBaseConfig();

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

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        log.debugf("Created session: %s\n", session);
        // Request
        replyQueue = session.createQueue(dynamicConfigQueue);
        requestQueue = session.createQueue(dynamicConfigQueue + "Requests");
        MessageConsumer consumer = session.createConsumer(requestQueue);
        keepaliveProducer = session.createProducer(requestQueue);
        producer = session.createProducer(replyQueue);

        Thread keepaliveThread = new Thread(this::keepaliveRequest, "KeepalivePump");
        keepaliveThread.start();
        while(running) {
            Message request = consumer.receive(10000);
            if (request != null) {
                if(request.propertyExists("keepalive"))
                    continue;
                handleRequest(request);
            }
        }
        log.info("Exiting?");
    }

    private void loadBaseConfig() throws IOException {
        URL scURL = getClass().getResource("/scannersConfig.json");
        InputStream is = scURL.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder tmp = new StringBuilder();
        String line = br.readLine();
        while(line != null) {
            tmp.append(line);
            line = br.readLine();
        }
        br.close();
        String json = tmp.toString();
        baseConfig = Utils.parseConfig(json);
        baseConfig.init();
        log("Loaded config: %s\n", baseConfig);
    }

    private void handleRequest(Message request) throws JMSException {
        int interfaceCount = request.getIntProperty("interfaceCount");
        for(int n = 0; n < interfaceCount; n ++) {
            String key0 = "mac" + n;
            String mac = request.getStringProperty(key0);
            String key1 = "addressCount" + n;
            int addressCount = request.getIntProperty(key1);
            for(int i = 0; i < addressCount; i ++) {
                String key2 = "ip"+n;
                String hostAddress = request.getStringProperty(key2);
                Properties properties = baseConfig.nextUnassignedScannerProperties(mac, hostAddress);
                log("handleRequest(%s,%s) = %s", mac, hostAddress, properties);
                MapMessage map = session.createMapMessage();
                if(properties == null) {
                    map.setBoolean("unavailable", true);
                } else {
                    for (String key : properties.stringPropertyNames()) {
                        map.setStringProperty(key, properties.getProperty(key));
                    }
                }
                producer.send(map);
                break;
            }
        }
    }
    private void log(String format, Object... args) {
        log.infof(format, args);
        if(console != null) {
            String text = String.format(format+"\n", args);
            console.write(text);
        }
        
    }
}
