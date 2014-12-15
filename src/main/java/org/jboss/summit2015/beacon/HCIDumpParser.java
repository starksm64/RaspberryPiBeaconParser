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

import com.beust.jcommander.JCommander;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the hcidump -R output for an interface in scan mode from a
 * 'sudo hcitool lescan --duplicates' command.
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class HCIDumpParser {
   private static Logger log = Logger.getLogger(HCIDumpParser.class);
   private static final int UUID_START = 69;
   private static final int MANUFACTURER_CODE_START = 57;
   private static final int BEACON_CODE_START = 63;
   private static final int MAJOR_START = 117;
   private static final int MINOR_START = 123;
   private static final int POWER_START = 129;
   private static final int RSSI_START = 132;
   /* This is an iBeacon line, BDADDR=0F:12:38:A5:8F:17 LE mm m0
   > 04 3E 2A 02 01 03 01 D5 9C E9 CD 56 3C 1E 02 01 06 1A FF 4C
     m1 c0.c1 uuid                    ...                uuid major
     00 02 15 DA F2 46 CE 83 63 11 E4 B1 16 12 3B 93 F7 5C BA 00
     minor pw rs
     00 00 01 C2 CE
     BDADDR = the beacon address
     LE = the advertising packet length
     mm = fixed code to indicate the manufacturer data ad
     m0..m1 = manufacturer code
     c0..c1 = beacon type code
     uuid..uuid = the proximity uuid
     major = iBeacon major number
     minor = iBeacon minor number
     pw = transmitted power calibration (+256)
     rs = RSSI (+256)
    */
   //                                                    BDADDR                    ADFlags                    iBeacon...
   private static final String IBEACON_FORMAT = ">\\s04.*((?:\\s\\p{XDigit}{2}){6})((?:\\s\\p{XDigit}{2}){4}) 1A FF 4C";
   /*
   TODO
    */
   private static final String ALTBEACON_FORMAT = "^4 ...";
   private Pattern ibeaconRE;
   private volatile boolean running;
   private CommandArgs cmdArgs;
   private MqttPublisher publisherClient;

   public static void main(String[] args) throws IOException, MqttException {
      CommandArgs cmdArgs = new CommandArgs();
      JCommander jc = new JCommander(cmdArgs);
      jc.parse(args);
      HCIDumpParser parser = new HCIDumpParser(cmdArgs);
      parser.scanHCI();
   }

   public HCIDumpParser(CommandArgs cmdArgs) {
      this.cmdArgs = cmdArgs;
   }

   private InputStream getInputStream() throws IOException {
      InputStream is = System.in;
      if(cmdArgs.rawDumpFile != null)
         is = new FileInputStream(cmdArgs.rawDumpFile);
      return is;
   }

   public void scanHCI() throws IOException, MqttException {
      running = true;
      ibeaconRE = Pattern.compile(IBEACON_FORMAT);
      setupPublisherClient();
      // Read from stdin
      InputStreamReader isr = new InputStreamReader(getInputStream());
      BufferedReader br = new BufferedReader(isr);
      String line = br.readLine();
      StringBuilder packet = new StringBuilder();
      log.info("Begining read of of hci raw dump stream");
      while(running && line != null) {
         line = line.trim();
         Matcher matcher = ibeaconRE.matcher(line);
         if(matcher.matches()) {
            packet.append(line.substring(2));
            String bdaddr = matcher.group(1);
            line = br.readLine();
            packet.append(' ');
            packet.append(line.trim());
            line = br.readLine();
            packet.append(' ');
            packet.append(line.trim());
            Beacon beacon = parseBeacon(packet);
            byte[] msg = beacon.toByteMsg();
            publisherClient.queueForPublish(cmdArgs.topicName, MqttQOS.AT_MOST_ONCE, msg);
         } else if(line.startsWith("> 04")) {
            log.debugf("No match: %s", line);
            line = br.readLine();
            line = br.readLine();
            if(line.startsWith("> 04"))
               continue;
         } else {
            log.debugf("Skipping: %s", line);
         }
         line = br.readLine();
         packet.setLength(0);
      }
      log.info("+++ End of scan");
   }

   private void setupPublisherClient() throws IOException, MqttException {
      publisherClient = new MqttPublisher(cmdArgs.brokerURL, cmdArgs.username, cmdArgs.password, cmdArgs.clientID);
      publisherClient.start();
   }

   private Beacon parseBeacon(StringBuilder packet) {
      StringBuilder uuid = new StringBuilder();
      for(int n = UUID_START; n < UUID_START+2*16+15; n ++) {
         char c = packet.charAt(n);
         if(c != ' ')
            uuid.append(c);
      }
      int manufacturer = 256*Integer.parseInt(packet.substring(MANUFACTURER_CODE_START, MANUFACTURER_CODE_START + 2), 16);
      manufacturer += Integer.parseInt(packet.substring(MANUFACTURER_CODE_START+3, MANUFACTURER_CODE_START + 5), 16);
      String code0 = packet.substring(BEACON_CODE_START, BEACON_CODE_START + 2);
      String code1 = packet.substring(BEACON_CODE_START+3, BEACON_CODE_START + 5);
      int code = 256*Integer.parseInt(code0, 16);
      code += Integer.parseInt(code1, 16);
      String major = packet.substring(MAJOR_START, MAJOR_START + 5);
      String minor = packet.substring(MINOR_START, MINOR_START + 5);
      String power = packet.substring(POWER_START, POWER_START+2);
      String rssi = packet.substring(RSSI_START, RSSI_START + 2);
      int imajor = 256*Integer.parseInt(major.substring(0, 2), 16) + Integer.parseInt(major.substring(3, 5), 16);
      int iminor = 256*Integer.parseInt(minor.substring(0, 2), 16) + Integer.parseInt(minor.substring(3, 5), 16);
      int ipower = Integer.parseInt(power, 16);
      ipower -= 256;
      int irssi = Integer.parseInt(rssi, 16);
      irssi -= 256;
      System.out.printf("UUID: %s, MF_Code: 0X%X, Becon_Code: 0X%X, Major/Minor(%s,%s/%d,%d), Power: %s(%d), RSSI: %s(%d)\n",
         uuid,
         manufacturer,
         code,
         major,
         minor,
         imajor,
         iminor,
         power,
         ipower,
         rssi,
         irssi
      );
      Beacon beacon = new Beacon(uuid.toString(), code, manufacturer, imajor, iminor, ipower, irssi);
      return beacon;
   }
}
