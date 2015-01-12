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
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Simple streaming of the output from the hcidump -R command executed on a remote host over ssh.
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TstJSch {
   public static void main(String[] arg) {
      int port = 22;
      try {
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
         JSch jsch = new JSch();

         String host = "192.168.1.95";
         if (arg.length > 0) {
            host = arg[0];
         }
         String user = "root";

         Session session = jsch.getSession(user, host, port);

         // sshUsername and sshPassword passed in via UserInfo interface.
         session.setUserInfo(new UserInfo() {
            @Override
            public String getPassphrase() {
               return null;
            }

            @Override
            public String getPassword() {
               return "raspberrypi";
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
         session.connect();
         ChannelExec channel = (ChannelExec) session.openChannel("exec");
         channel.setCommand("/usr/local/bin/hcidump -R");
         channel.setInputStream(null);
         channel.setErrStream(System.err);

         InputStream in = channel.getInputStream();
         channel.connect();
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         int count = 0;
         String line = br.readLine();
         while (line != null) {
            System.out.printf("%d: %s\n", count++, line);
            line = br.readLine();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
