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
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconConfiguration {
   private int id;
   private String name;
   private String type;
   private String proximityUUID;
   private String antennaType;
   private int major;
   private int minor;
   private String measuredPower;
   private String transmissionPower;
   private String optimizedForBackground;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @XmlElement(name = "beacon_type")
   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   @XmlElement(name="proximity_uuid")
   public String getProximityUUID() {
      return proximityUUID;
   }

   public void setProximityUUID(String proximityUUID) {
      this.proximityUUID = proximityUUID;
   }

   @XmlElement(name="antenna_type")
   public String getAntennaType() {
      return antennaType;
   }

   public void setAntennaType(String antennaType) {
      this.antennaType = antennaType;
   }

   public int getMajor() {
      return major;
   }

   public void setMajor(int major) {
      this.major = major;
   }

   public int getMinor() {
      return minor;
   }

   public void setMinor(int minor) {
      this.minor = minor;
   }

   public int getMeasuredPowerAsInt() {
      int power = 0;
      if(!measuredPower.equalsIgnoreCase("Recommended"))
         power = Integer.valueOf(measuredPower);
      return power;
   }
   @XmlElement(name="measured_power")
   public String getMeasuredPower() {
      return measuredPower;
   }

   public void setMeasuredPower(String measuredPower) {
      this.measuredPower = measuredPower;
   }

   public int getTransmissionPowerAsInt() {
      int power = 0;
      if(!transmissionPower.equalsIgnoreCase("Recommended"))
         power = Integer.valueOf(transmissionPower);
      return power;
   }
   @XmlElement(name="transmission_power")
   public String getTransmissionPower() {
      return transmissionPower;
   }

   public void setTransmissionPower(String transmissionPower) {
      this.transmissionPower = transmissionPower;
   }

   @XmlElement(name="optimized_for_background")
   public String getOptimizedForBackground() {
      return optimizedForBackground;
   }

   public void setOptimizedForBackground(String optimizedForBackground) {
      this.optimizedForBackground = optimizedForBackground;
   }

   @Override
   public String toString() {
      return "BeaconConfiguration{" +
         "id=" + id +
         ", name='" + name + '\'' +
         ", type='" + type + '\'' +
         ", proximityUUID='" + proximityUUID + '\'' +
         ", antennaType='" + antennaType + '\'' +
         ", major=" + major +
         ", minor=" + minor +
         ", measuredPower='" + measuredPower + '\'' +
         ", transmissionPower=" + transmissionPower +
         '}';
   }
}
