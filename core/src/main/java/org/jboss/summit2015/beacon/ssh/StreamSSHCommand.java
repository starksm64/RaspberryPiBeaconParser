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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.jboss.summit2015.beacon.ParseCommand;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Parameters(commandDescription = "Expose the hcidump output to as a stream over ssh")
public class StreamSSHCommand {
   @Parameter(names = "-port",
      description = "Specify the SSHD port to connect to")
   public int port = 22;
   @Parameter(names = "-host",
      description = "Specify the SSHD host")
   public String host = null;
   @Parameter(names = "-sshUsername",
      description = "Specify the SSHD sshUsername")
   public String sshUsername = "root";
   @Parameter(names = "-sshPassword",
      description = "Specify the SSHD sshPassword")
   public String sshPassword = "raspberrypi";
   @Parameter(names = "-hcidump",
      description = "Specify the hcidump command to run")
   public String hcidump = "/usr/local/bin/hcidump -R";

   @Parameter(names = "-verbose",
      description = "Enable verbose mode on the SSH connection")
   public boolean verbose = false;

   @ParametersDelegate
   public ParseCommand parserCmd = new ParseCommand();
}
