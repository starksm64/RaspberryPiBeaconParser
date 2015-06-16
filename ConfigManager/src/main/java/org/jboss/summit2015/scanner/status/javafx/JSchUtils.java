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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
