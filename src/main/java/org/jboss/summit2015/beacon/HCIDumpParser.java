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
import org.jboss.summit2015.beacon.ssh.SSHStreamParser;
import org.jboss.summit2015.beacon.ssh.StreamSSHCommand;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Parser for the hcidump -R output for an interface in scan mode from a
 * 'sudo hcitool lescan --duplicates' command.
 *
 * Enable the jboss logmanager by passing -Djava.util.logging.manager=org.jboss.logmanager.LogManager to
 * the vm arguments. To specify an override to the bundled logging.properties, use
 * -Dlogging.configuration=file:path-to-logging.properties
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class HCIDumpParser {
   private static Logger log = Logger.getLogger(HCIDumpParser.class);
   private volatile boolean running;
   private CommandArgs cmdArgs;
   private LinkedBlockingDeque<Beacon> beacons;
   private AbstractParser parser;

   public static void main(String[] args) throws Exception {
      HCIDumpParser parser = new HCIDumpParser();
      parser.run(args);
   }

   public LinkedBlockingDeque<Beacon> getBeacons() {
      return beacons;
   }
   public void setBeacons(LinkedBlockingDeque<Beacon> beacons) {
      this.beacons = beacons;
   }

   public void run(String[] args) throws Exception {
      running = true;
      ParseCommand parse = new ParseCommand();
      StreamCommand stream = new StreamCommand();
      StreamSSHCommand streamSSH = new StreamSSHCommand();
      cmdArgs = new CommandArgs();
      JCommander jc = new JCommander(cmdArgs);

      // Command modes
      jc.addCommand("parse", parse);
      jc.addCommand("stream", stream);
      jc.addCommand("stream-ssh", streamSSH);

      jc.parse(args);
      String command = jc.getParsedCommand();
      if(command.equals("parse"))
         scanHCI(parse);
      else if(command.equals("stream"))
         streamHCI(stream);
      else if(command.equals("stream-ssh"))
         streamHCI_SSH(streamSSH);
   }
   public void stop() {
      parser.setRunning(false);
      parser.cleanup();
   }

   public void streamHCI(StreamCommand streamCmd) throws IOException {
      log.info("Begin streamHCI");
      StreamServer streamServer = new StreamServer(streamCmd);
      parser = streamServer;
      InputStream is = streamServer.getInputStream(cmdArgs.rawDumpFile);
      streamServer.processHCIStream(is);
      streamServer.cleanup();
      log.info("End streamHCI");
   }
   public void streamHCI_SSH(StreamSSHCommand streamCmd) throws Exception {
      log.info("Begin streamHCI_SSH");
      SSHStreamParser streamServer = new SSHStreamParser(streamCmd);
      parser = streamServer;
      streamServer.setBeacons(beacons);
      InputStream is = streamServer.getInputStream(cmdArgs.rawDumpFile);
      streamServer.processHCIStream(is);
      streamServer.cleanup();
      log.info("End streamHCI_SSH");
   }
   public void scanHCI(ParseCommand parseCmd) throws IOException, MqttException {
      log.info("Begin scanHCI");
      ParserLogic streamParser = new ParserLogic(parseCmd);
      parser = streamParser;
      streamParser.setBeacons(beacons);
      InputStream is = streamParser.getInputStream(cmdArgs.rawDumpFile);
      streamParser.processHCIStream(is);
      streamParser.cleanup();
      log.info("End scanHCI");
   }

}
