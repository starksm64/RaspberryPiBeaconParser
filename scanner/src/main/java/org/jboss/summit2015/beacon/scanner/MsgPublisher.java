package org.jboss.summit2015.beacon.scanner;

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

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public interface MsgPublisher {
   enum MsgPublisherType {
      PAHO_MQTT,
      AMQP_PROTON,
      AMQP_CMS
   };

   public void start(boolean asyncMode) throws Exception;
   public void stop() throws Exception;
   public void queueForPublish(String topicName, MqttQOS qos, byte[] payload) throws Exception;
   public void publish(String topicName, MqttQOS qos, byte[] payload) throws Exception;
}
