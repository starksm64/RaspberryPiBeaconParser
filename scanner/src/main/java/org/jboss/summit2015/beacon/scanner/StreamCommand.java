package org.jboss.summit2015.beacon.scanner;
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

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Parameters(commandDescription = "Expose the hcidump output to as a socket stream")
public class StreamCommand {

   public static class InetAddressConverter implements IStringConverter<InetAddress> {
      static InetAddress getLocalHost() {
         try {
            return InetAddress.getLocalHost();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      public InetAddress convert(String value) {
         try {
            return InetAddress.getByName(value);
         } catch (UnknownHostException e) {
            throw new ParameterException(e);
         }
      }
   }

   @Parameter(names = "-port",
      description = "Specify the listening port")
   public int port = 12345;
   @Parameter(names = "-backlog",
      description = "Specify the accept backlog")
   public int backlog = 1;
   @Parameter(names = "-bindAddr", converter = InetAddressConverter.class,
      description = "Specify the listening interface")
   public InetAddress bindAddr = InetAddressConverter.getLocalHost();
   @Parameter(names = "-linesBacklog",
      description = "Specify the max number of lines to maintain when there is no active client")
   public int linesBacklog = 10000;

}
