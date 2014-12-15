package org.jboss.summit2015.beacon;
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
	 * Quality of service for a message.
	 * <ul>
	 * <li>Quality of Service 0 - indicates that a message should
	 * be delivered at most once (zero or one times).  The message will not be persisted to disk,
	 * and will not be acknowledged across the network.  This QoS is the fastest,
	 * but should only be used for messages which are not valuable - note that
	 * if the server cannot process the message (for example, there
	 * is an authorization problem), then an
	 * {@link org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)}.
	 * Also known as "fire and forget".</li>
	 *
	 * <li>Quality of Service 1 - indicates that a message should
	 * be delivered at least once (one or more times).  The message can only be delivered safely if
	 * it can be persisted, so the application must supply a means of
	 * persistence using <code>MqttConnectOptions</code>.
	 * If a persistence mechanism is not specified, the message will not be
	 * delivered in the event of a client failure.
	 * The message will be acknowledged across the network.
	 * This is the default QoS.</li>
	 *
	 * <li>Quality of Service 2 - indicates that a message should
	 * be delivered once.  The message will be persisted to disk, and will
	 * be subject to a two-phase acknowledgement across the network.
	 * The message can only be delivered safely if
	 * it can be persisted, so the application must supply a means of
	 * persistence using <code>MqttConnectOptions</code>.
	 * If a persistence mechanism is not specified, the message will not be
	 * delivered in the event of a client failure.</li>
	 *
	 * If persistence is not configured, QoS 1 and 2 messages will still be delivered
	 * in the event of a network or server problem as the client will hold state in memory.
	 * If the MQTT client is shutdown or fails and persistence is not configured then
	 * delivery of QoS 1 and 2 messages can not be maintained as client-side state will
	 * be lost.

 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public enum MqttQOS {
   // QOS = 0
   AT_MOST_ONCE,
   // OQS = 1
   AT_LEAST_ONCE,
   // QOS = 2
   EXACTLY_ONCE
}
