package org.jboss.summit2015.beacon.ssh;
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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jboss.logging.Logger;
import org.jboss.summit2015.beacon.AbstractParser;
import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.beacon.ParserLogic;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class SSHStreamParser extends AbstractParser {
   private static Logger log = Logger.getLogger(SSHStreamParser.class);

   private StreamSSHCommand streamCmd;
   private ChannelExec channel;
   private Session session;
   private LinkedBlockingDeque<Beacon> beacons;
   private ParserLogic parser;

   public SSHStreamParser(StreamSSHCommand streamCmd) {
      this.streamCmd = streamCmd;
   }

   @Override
   public boolean isRunning() {
      return parser.isRunning();
   }

   @Override
   public void setRunning(boolean running) {
      parser.setRunning(running);
   }

   @Override
   public InputStream getInputStream(String testDataPath) throws IOException {
      if (testDataPath != null)
         return super.getInputStream(testDataPath);

      // Enable verbose logging
      if (streamCmd.verbose) {
         JSch.setLogger(new com.jcraft.jsch.Logger() {
                           public boolean isEnabled(int level) {
                              return true;
                           }

                           public void log(int level, String message) {
                              log.debugf("%d: %s\n", level, message);
                           }
                        }
         );
      }
      JSch jsch = new JSch();
      InputStream is = null;
      try {
         session = jsch.getSession(streamCmd.sshUsername, streamCmd.host, streamCmd.port);

         // sshUsername and sshPassword passed in via UserInfo interface.
         session.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
               return null;
            }

            @Override
            public String getPassword() {
               return streamCmd.sshPassword;
            }

            @Override
            public boolean promptPassword(String message) {
               return true;
            }

            @Override
            public boolean promptPassphrase(String message) {
               return false;
            }

            @Override
            public boolean promptYesNo(String message) {
               return true;
            }

            @Override
            public void showMessage(String message) {
               log.infof("showMessage: %s\n", message);
            }
         });
         session.connect();
         channel = (ChannelExec) session.openChannel("exec");
         channel.setCommand(streamCmd.hcidump);
         channel.setInputStream(null);
         channel.setErrStream(System.err);
         is = channel.getInputStream();
         channel.connect();
      } catch (JSchException e) {
         e.printStackTrace();
      }
      return is;
   }

   @Override
   public void processHCIStream(final InputStream is) throws IOException, MqttException {
      parser = new ParserLogic(streamCmd.parserCmd);
      parser.setScannerID(getScannerID());
      parser.setBeacons(beacons);
      parser.processHCIStream(is);
   }

   @Override
   public void cleanup() {
      if(channel != null) {
         channel.disconnect();
         channel = null;
      }
      if(session != null) {
         session.disconnect();
         session = null;
      }
      if(parser != null) {
         parser.cleanup();
         parser = null;
      }
   }
   public LinkedBlockingDeque<Beacon> getBeacons() {
      return beacons;
   }

   /**
    * Set a queue to collect the beacons. Used when one is not immediately sending the beacons to the MQTT server.
    * @param beacons
    */
   public void setBeacons(LinkedBlockingDeque<Beacon> beacons) {
      this.beacons = beacons;
   }
}
