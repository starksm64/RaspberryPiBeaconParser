package org.jboss.summit2015.beacon;/*
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

import org.jboss.summit2015.beacon.MsgPublisher.MsgPublisherType;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class MsgPublisherFactory {
   public static MsgPublisher create(MsgPublisherType type, String brokerUrl, String userName, String password, String clientID) {
      MsgPublisher publisher = null;
      switch (type) {
         case PAHO_MQTT:
            publisher = new MqttPublisher(brokerUrl, userName, password, clientID);
            break;
         case AMQP_PROTON:
            break;
         case AMQP_CMS:
            break;
      }
      return publisher;
   }
}
