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
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Execute commands on scanners using ssh
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TstJSch {
   static JSch jsch;
   static Session session;

   @BeforeClass
   public static void init() throws Exception {
      // Enable verbose logging
      JSch.setLogger(new com.jcraft.jsch.Logger(){
                        public boolean isEnabled(int level){
                           return true;
                        }
                        public void log(int level, String message){
                           System.out.printf("%d: %s\n", level, message);
                        }
                     }
      );
      jsch = new JSch();
      String host = "192.168.1.142";
      String user = "root";

      ConfigRepository configRepository = OpenSSHConfig.parseFile("~/.ssh/config");
      jsch.setConfigRepository(configRepository);
      session = jsch.getSession(user, host);
      // sshUsername and sshPassword passed in via UserInfo interface.
      session.setUserInfo(new UserInfo() {
         @Override
         public String getPassphrase() {
            return null;
         }

         @Override
         public String getPassword() {
            return "root0FPi";
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
            System.out.printf("showMessage: %s\n", message);
         }
      });
   }

   @Test
   public void testAllStatus() throws Exception {
      session.connect();
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand("systemctl --full status test-network.timer test-network.service scannerd.service lescan.service");
      readOutput(channel);
      session.disconnect();
   }

   @Test
   public void testTestNetworkTimerStatus() throws Exception {
      session.connect();
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand("systemctl --full status test-network.timer");
      readOutput(channel);
      session.disconnect();
   }

   @Test
   public void testTestNetworkTimerRestart() throws Exception {
      session.connect();
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand("systemctl --full restart test-network.timer");
      readOutput(channel);
      session.disconnect();
   }

    @Test
    public void testGitRepoUpdate() throws Exception {
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("cd ~/NativeRaspberryPiBeaconParser; git pull");
        readOutput(channel);
        session.disconnect();
    }

    @Test
    public void testUpdateService() throws Exception {
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("cd ~/NativeRaspberryPiBeaconParser; git pull");
        readOutput(channel);
        session.disconnect();
    }

    private void readOutput(ChannelExec channel) throws IOException, JSchException {
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int count = 0;
        String line = br.readLine();
        while (line != null && line.length() > 0) {
            System.out.printf("%d: %s\n", count++, line);
            line = br.readLine();
        }

    }
}
