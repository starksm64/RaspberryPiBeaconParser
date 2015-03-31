package gimbal;
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

import javax.xml.bind.annotation.XmlElement;

/**
 {
     "assigned_configuration": "Configuration assigned to the beacon",
     "applied_configuration": "Configuration applied to the beacon"
 }
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconConfigurationInfo {
   private String assignedConfiguration;
   private String appliedConfiguration;


   @XmlElement(name = "assigned_configuration")
   public String getAssignedConfiguration() {
      return assignedConfiguration;
   }

   public void setAssignedConfiguration(String assignedConfiguration) {
      this.assignedConfiguration = assignedConfiguration;
   }

   @XmlElement(name = "applied_configuration")
   public String getAppliedConfiguration() {
      return appliedConfiguration;
   }

   public void setAppliedConfiguration(String appliedConfiguration) {
      this.appliedConfiguration = appliedConfiguration;
   }

   @Override
   public String toString() {
      return "BeaconConfigurationInfo{" +
         "assignedConfiguration='" + assignedConfiguration + '\'' +
         ", appliedConfiguration='" + appliedConfiguration + '\'' +
         '}';
   }
}
