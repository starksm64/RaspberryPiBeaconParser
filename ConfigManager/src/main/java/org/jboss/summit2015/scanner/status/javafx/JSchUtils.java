package org.jboss.summit2015.scanner.status.javafx;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import javafx.beans.property.StringProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * Created by starksm on 6/3/15.
 */
public class JSchUtils {
    public static String getServicesStatus(String host, TextStream out) throws JSchException, IOException {
        Session session = getDefaultSession(host);
        String output = execCommand(session, out, "systemctl --full status test-network.timer test-network.service scannerd.service lescan.service");
        return output;
    }

    public static String updateGitRepo(String host, TextStream out) throws JSchException, IOException {
        Session session = getDefaultSession(host);
        String output = execCommand(session, out, "cd ~/NativeRaspberryPiBeaconParser; git pull");
        return output;
    }

    public static String checkScannerConfig(String host, TextStream out) throws JSchException, IOException {
        Session session = getDefaultSession(host);
        String output = execCommand(session, out, "java -jar /usr/local/bin/ConfigManager-service.jar -dryRun");
        return output;
    }

    public static void scpToRemote(String host, File lfile, String remotePath, TextStream tout) throws JSchException, IOException {
        Session session = getDefaultSession(host);
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("scp -t "+remotePath);

        // get I/O streams for remote scp
        OutputStream out=channel.getOutputStream();
        InputStream in=channel.getInputStream();

        channel.connect();

        if(checkAck(in)!=0){
            System.exit(0);
        }

        // send "C0644 filesize filename", where filename should not include '/'
        long filesize=lfile.length();
        String command="C0644 "+filesize+" " + lfile.getName();
        command+="\n";
        out.write(command.getBytes()); out.flush();
        int ok = checkAck(in);
        if(ok != 0){
            throw new JSchException("checkAck failure, "+ok);
        }
        tout.appendText(String.format("Transferring %d bytes from: %s\n", filesize, lfile.getAbsolutePath()));

        // send a content of lfile
        FileInputStream fis = new FileInputStream(lfile);
        byte[] buf = new byte[1024];
        int len = fis.read(buf, 0, buf.length);
        long transferred = len;
        long tenPct = filesize / 10;
        int count = 0;
        while(len > 0){
            out.write(buf, 0, len);
            len = fis.read(buf, 0, buf.length);
            transferred += len;
            if(transferred >= tenPct) {
                count ++;
                tenPct = transferred + filesize / 10;
                tout.appendText(String.format("%d%%\n", 10*count));
            }
        }
        fis.close();

        // send '\0'
        buf[0]=0;
        out.write(buf, 0, 1);
        out.flush();
        ok = checkAck(in);
        if(ok != 0){
            throw new JSchException("checkAck failure, "+ok);
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


    private static Session getDefaultSession(String host) throws JSchException, IOException {
        // Enable verbose logging
        JSch.setLogger(new com.jcraft.jsch.Logger() {
                           public boolean isEnabled(int level) {
                               return true;
                           }

                           public void log(int level, String message) {
                               System.out.printf("%d: %s\n", level, message);
                           }
                       }
        );
        JSch jsch = new JSch();
        String user = "root";

        ConfigRepository configRepository = OpenSSHConfig.parseFile("~/.ssh/config");
        jsch.setConfigRepository(configRepository);
        Session session = jsch.getSession(user, host);
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
        session.connect();
        return session;
    }

    private static String execCommand(Session session, TextStream out, String command) throws JSchException, IOException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        int count = 0;
        String line = br.readLine();
        StringWriter sw = new StringWriter();
        while (line != null) {
            String info = String.format("%d: %s\n", count++, line);
            sw.append(info);
            out.appendText(info);
            line = br.readLine();
        }
        session.disconnect();
        return sw.toString();
    }
}
