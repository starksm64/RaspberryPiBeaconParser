package org.jboss.summit2015.config;
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

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class Utils {


   /**
    * Return a list of all NetworkInterface hardware addresses in %x:%x:%x:%x:%x:%x format
    * @return NetworkInterface hardware addresses
    * @throws SocketException
    */
   public static List<String> getAllHardwareAddress() throws SocketException {
      ArrayList<String> addresses = new ArrayList<>();
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      while(ifaces.hasMoreElements()) {
         NetworkInterface iface = ifaces.nextElement();
         byte[] macaddr = iface.getHardwareAddress();
         if(macaddr == null)
            continue;
         String key = String.format("%x:%x:%x:%x:%x:%x", macaddr[0], macaddr[1], macaddr[2], macaddr[3], macaddr[4], macaddr[5]);
         addresses.add(key);
      }
      return addresses;
   }

   /**
    * Locate the scanner properties from the base config using the jvm network interface hardware addresses
    * @param config
    * @return the scannerProperties value with a matching macaddr property
    */
   public static Properties findPropertiesByHardwareAddress(BaseConfig config) throws SocketException {
      Properties properties = null;
      Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
      while(properties == null && ifaces.hasMoreElements()) {
         NetworkInterface iface = ifaces.nextElement();
         byte[] macaddr = iface.getHardwareAddress();
         if(macaddr == null)
            continue;
         String key = String.format("%x:%x:%x:%x:%x:%x", macaddr[0], macaddr[1], macaddr[2], macaddr[3], macaddr[4], macaddr[5]);
         properties = config.findPropertiesByHardwareAddress(key);
      }
      return properties;
   }
}
