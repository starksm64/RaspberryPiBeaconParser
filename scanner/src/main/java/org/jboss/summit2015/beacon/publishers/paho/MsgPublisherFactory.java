package org.jboss.summit2015.beacon.publishers.paho;

import org.jboss.summit2015.beacon.common.MsgPublisher;
import org.jboss.summit2015.beacon.common.MsgPublisherType;

/**
 * Created by starksm on 7/12/15.
 */
public class MsgPublisherFactory implements org.jboss.summit2015.beacon.common.MsgPublisherFactory {
    public MsgPublisher create(MsgPublisherType type, String brokerUrl, String userName, String password, String clientID) {
        MsgPublisher publisher = null;
        switch (type) {
            case PAHO_MQTT:
                publisher = new MqttPublisher(brokerUrl, userName, password, clientID);
                break;
        }
        return publisher;
    }
}
