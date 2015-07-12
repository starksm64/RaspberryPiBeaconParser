package org.jboss.summit2015.beacon.common;/*
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
@FunctionalInterface
public interface MsgPublisherFactory {
   public MsgPublisher create(MsgPublisherType type, String brokerUrl, String userName, String password, String clientID);

    public static MsgPublisher newMsgPublisher(MsgPublisherType type, String brokerUrl,
                                               String userName, String password, String clientID)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String factoryClassName = type.getFactoryClass();
        if(factoryClassName == null)
            throw new IllegalStateException("No factory class specified for: "+type);
        Class<MsgPublisherFactory> factoryClass = (Class<MsgPublisherFactory>) Class.forName(factoryClassName);
        MsgPublisherFactory factory = factoryClass.newInstance();
        return factory.create(type, brokerUrl, userName, password, clientID);
    }
}
