package org.jboss.summit2015.beacon.common;

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

import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.beacon.bluez.BeaconInfo;
import org.jboss.summit2015.beacon.scanner.MqttQOS;

import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public interface MsgPublisher {

    void setDestinationName(String name);
    String getDestinationName();

    int getReconnectInterval();
    void setReconnectInterval(int reconnectInterval);

    boolean isReconnectOnFailure();

    void setReconnectOnFailure(boolean reconnectOnFailure);

    boolean isConnected();
    void setConnected(boolean connected);

    /**
     * Set whether the destinationName represents a topic.
     * @param flag - true for a topic, false for a queue
     */
    void setUseTopics(boolean flag);
    boolean isUseTopics();

    boolean isUseTransactions();
    void setUseTransactions(boolean useTransactions);
    public void start(boolean asyncMode) throws Exception;

    public void stop() throws Exception;

    public void queueForPublish(String topicName, MqttQOS qos, byte[] payload) throws Exception;

    public void publish(String topicName, MqttQOS qos, byte[] payload) throws Exception;
    public void publish(String destinationName, BeaconInfo beaconInfo);
    public void publish(String destinationName, Beacon beacon);
    public void publishStatus(BeaconInfo beaconInfo);
    public void publishStatus(Beacon beacon);
    public void publishProperties(String destinationName, Properties properties);
}
