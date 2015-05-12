package org.jboss.summit2015.replay;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Singleton
@Named
public class MsgPublisher implements Serializable {
   private static final Logger log = Logger.getLogger(MsgPublisher.class);
   private static volatile int clientCount = 0;

   private String brokerURL = "amqp://52.10.252.216:5672";
   private String destinationName = "beaconEvents";
   private transient Connection connection;
   private transient Session session;
   private transient MessageProducer producer;
   private String clientID;

   static void populateMessage(Message message, JsonObject beacon) throws JMSException {
      long time = beacon.getAsJsonPrimitive("time").getAsLong();
      populateMessage(message, beacon, time);
   }
   static void populateMessage(Message message, JsonObject beacon, long time) throws JMSException {
      message.setStringProperty("uuid", beacon.getAsJsonPrimitive("uuid").getAsString());
      message.setStringProperty("scannerID", beacon.getAsJsonPrimitive("scannerID").getAsString());
      message.setIntProperty("major", beacon.getAsJsonPrimitive("major").getAsInt());
      message.setIntProperty("minor", beacon.getAsJsonPrimitive("minor").getAsInt());
      message.setIntProperty("manufacturer", beacon.getAsJsonPrimitive("manufacturer").getAsInt());
      message.setIntProperty("code", beacon.getAsJsonPrimitive("code").getAsInt());
      message.setIntProperty("power", beacon.getAsJsonPrimitive("power").getAsInt());
      message.setIntProperty("calibratedPower", beacon.getAsJsonPrimitive("calibratedPower").getAsInt());
      message.setIntProperty("rssi", beacon.getAsJsonPrimitive("rssi").getAsInt());
      message.setLongProperty("time", time);
      message.setIntProperty("messageType", beacon.getAsJsonPrimitive("messageType").getAsInt());
   }

   @PostConstruct
   public void init() {
      clientID = "ReplayService-" + clientCount ++;
      log.infof("Initialize messaging layer, brokerURL=%s", brokerURL);
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", brokerURL);

      try {
         Context context = new InitialContext(props);
         // Create a Connection
         ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
         connection = factory.createConnection("guest", "guest");
         //connection.setClientID(clientID);
         System.out.printf("ActiveMQConnectionFactory created connection: %s\n", connection);

         connection.setExceptionListener(ex -> log.error("Messaging connection error", ex));
         connection.start();

         session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
         //wsSession = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
         System.out.printf("Created wsSession: %s\n", session);

         // Create the message publisher sending to destinationName
         //Destination destination = wsSession.createQueue(destinationName);
         Destination destination = session.createTopic(destinationName);
         producer = session.createProducer(destination);
         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      } catch (Throwable e) {
         log.error("Failed to initialize messaging layer", e);
      }
      log.info("done");
   }
   @PreDestroy
   public void destroy() {
      try {
         log.infof("Destroy messaging layer, clientID=%s", connection.getClientID());
         close();
      } catch (Throwable e) {
         log.error("Failed to destroy messaging layer", e);
      }
   }

   public String getClientID() {
      return clientID;
   }

   public String getDestinationName() {
      return destinationName;
   }

   public void setDestinationName(String destinationName) {
      this.destinationName = destinationName;
      // Update the producer
      if(producer != null) {
         try {
            producer.close();
            Destination destination = session.createQueue(destinationName);
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
         } catch (JMSException e) {
            log.error("Failed to update producer", e);
         }
      }
   }

   public void commit() throws JMSException {
      session.commit();
   }
   public void close() {
      if(connection == null)
         return;

      try {
         log.infof("Closing messaging layer, clientID=%s", connection.getClientID());
         producer.close();
         session.close();
         connection.close();
      } catch (JMSException e) {
         log.error("Failed to destroy messaging layer", e);
      }
      producer = null;
      session = null;
      connection = null;
   }
   synchronized public void publishEvent(JsonObject event) throws JMSException {
      TextMessage message = session.createTextMessage();
      populateMessage(message, event);
      producer.send(message);
   }
   synchronized public void publishEvent(JsonObject event, long time) throws JMSException {
      TextMessage message = session.createTextMessage();
      populateMessage(message, event, time);
      producer.send(message);
   }
   public void publishTestMessage() throws JMSException {
      TextMessage message = session.createTextMessage("Hello JMS");
      message.setIntProperty("messageType", 10);
      producer.send(message);
   }

}
