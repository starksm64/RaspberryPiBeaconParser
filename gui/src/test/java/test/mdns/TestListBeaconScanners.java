package test.mdns;
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

import org.junit.Test;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestListBeaconScanners {
   /**
    * Query the mDNS envrionment for "_ssh._tcp.local." types as this is what the
    * /etc/avahi/services/beacon-scanner.service on the pidora image sets up with a formats=iBeacon property.
    */
   @Test
   public void queryBeaconScanners() {
      JmDNS jmdns = null;
      try {
         int count = 0;
         String type = "_ssh._tcp.local.";
         jmdns = JmDNS.create();
         while (count <= 0) {
            ServiceInfo[] infos = jmdns.list(type);
            System.out.printf("List %s\n", type);
            for (ServiceInfo info : infos) {
               System.out.println(info);
               System.out.printf("Full type: %s\n", info.getTypeWithSubtype());
               System.out.printf("Subtype: %s\n", info.getSubtype());
               String formats = info.getPropertyString("formats");
               if (formats != null && formats.equalsIgnoreCase("iBeacon")) {
                  System.out.printf("Found iBeacon scanner service based on formats\n");
                  count++;
               }
            }
            System.out.println();

            try {
               Thread.sleep(5000);
            } catch (InterruptedException e) {
               break;
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (jmdns != null) try {
            jmdns.close();
         } catch (IOException exception) {
            //
         }
      }
   }
}
