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

import org.jboss.logging.Logger;
import org.jboss.summit2015.scanner.status.StatusProperties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.websocket.Session;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class StatusMonitor implements Runnable {
   static Logger log = Logger.getLogger(StatusMonitor.class);
   final Session wsSession;
   final String destination;

   public StatusMonitor(Session session, String destination) {
      this.wsSession = session;
      this.destination = destination;
   }

   @Override
   public void run() {
      try {
         doRun();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void doRun() throws Exception {
      Properties props = new Properties();
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
      props.setProperty("connectionfactory.myFactoryLookup", "amqp://52.10.252.216:5672");
      //props.setProperty("connectionfactory.myFactoryLookup", "amqp://192.168.1.107:5672");

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
      connection.setClientID("scannerHealthMonitor");
      connection.start();

      javax.jms.Session session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
      System.out.printf("Created wsSession: %s\n", session);

      int exitCode = 0;
      Topic destination = session.createTopic("scannerHealth");
      TopicSubscriber monitor = session.createDurableSubscriber(destination, "scannerHealthMonitor");
      Message msg = monitor.receive();
      while(msg != null) {
         System.out.printf("Msg(%d)\n", msg.getJMSTimestamp());
         for (StatusProperties sp : StatusProperties.values()) {
            String value = msg.getStringProperty(sp.name());
            System.out.printf("\t%s=%s\n", sp, value);
         }
         for (Session s : wsSession.getOpenSessions()) {
            if (s.isOpen()) {
               // TODO
               s.getAsyncRemote().sendText("");
            }
         }
         msg = monitor.receive();
      }
      monitor.close();
      session.close();
      connection.close();

   }
}
