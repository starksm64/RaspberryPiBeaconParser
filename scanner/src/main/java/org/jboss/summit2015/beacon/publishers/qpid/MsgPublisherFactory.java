package org.jboss.summit2015.beacon.publishers.qpid;

import org.jboss.summit2015.beacon.common.MsgPublisher;
import org.jboss.summit2015.beacon.common.MsgPublisherType;

/**
 * TODO
 */
public class MsgPublisherFactory implements org.jboss.summit2015.beacon.common.MsgPublisherFactory {
    public MsgPublisher create(MsgPublisherType type, String brokerUrl, String userName, String password, String clientID) {
        MsgPublisher publisher = null;
        switch (type) {
            case AMQP_QPID:
                publisher = new QpidPublisher();
                break;
        }
        return publisher;
    }
}
