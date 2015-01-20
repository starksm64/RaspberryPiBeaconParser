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

import com.beust.jcommander.Parameter;
import java.io.Serializable;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class CommandArgs implements Serializable {
   private static final long serialVersionUID = 1;

   @Parameter(names = "--help", help = true)
   private boolean help;


   @Parameter(names = "-scannerID",
      description = "Specify the name of the scanner generating the beacon events")
   public String scannerID = "Default";

   @Parameter(names = "-rawDumpFile",
      description = "Specify a path to an hcidump file to parse for testing")
   public String rawDumpFile = null;
}
