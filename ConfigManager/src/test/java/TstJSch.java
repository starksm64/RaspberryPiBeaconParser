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
import org.jboss.summit2015.scanner.status.javafx.JSchUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
      String host = "192.168.1.47";
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

    @Test
    public void testSCP() throws Exception {
        session.connect();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("scp -t /usr/local/bin/ConfigManager-service.jar");

// get I/O streams for remote scp
        OutputStream out=channel.getOutputStream();
        InputStream in=channel.getInputStream();

        channel.connect();

        if(checkAck(in)!=0){
            System.exit(0);
        }

        File lfile = new File("/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/ConfigManager/build/libs/ConfigManager-service.jar");

        // send "C0644 filesize filename", where filename should not include '/'
        long filesize=lfile.length();
        String command="C0644 "+filesize+" " + lfile.getName();
        /*???
        if(lfile.lastIndexOf('/')>0){
            command+=lfile.substring(lfile.lastIndexOf('/')+1);
        }
        else{
            command+=lfile;
        }
        */
        command+="\n";
        out.write(command.getBytes()); out.flush();
        if(checkAck(in)!=0){
            System.exit(0);
        }

        // send a content of lfile
        FileInputStream fis=new FileInputStream(lfile);
        byte[] buf=new byte[1024];
        while(true){
            int len=fis.read(buf, 0, buf.length);
            if(len<=0) break;
            out.write(buf, 0, len); //out.flush();
        }
        fis.close();

        // send '\0'
        buf[0]=0;
        out.write(buf, 0, 1);
        out.flush();
        if(checkAck(in)!=0){
            throw new IOException("checkAck error");
        }
        out.close();

        channel.disconnect();
        session.disconnect();
}

    static int checkAck(InputStream in) throws IOException{
        int b=in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;

        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
                System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
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
