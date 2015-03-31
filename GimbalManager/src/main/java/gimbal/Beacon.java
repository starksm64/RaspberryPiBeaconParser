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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Beacon {
   String id;
   String factoryID;
   String iconURL;
   String name;
   float latitude;
   float longitude;
   String visibility;
   String batteryLevel;
   String hardware;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public int getIdFromName() {
      int end = name.length();
      int start = end - 1;
      while(Character.isDigit(name.charAt(start))) {
         start --;
      }
      return Integer.parseInt(name.substring(start+1, end));
   }

   @XmlElement(name = "factory_id")
   public String getFactoryID() {
      return factoryID;
   }

   public void setFactoryID(String factoryID) {
      this.factoryID = factoryID;
   }


   @XmlElement(name = "icon_url")
   public String getIconURL() {
      return iconURL;
   }

   public void setIconURL(String iconURL) {
      this.iconURL = iconURL;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public float getLatitude() {
      return latitude;
   }

   public void setLatitude(float latitude) {
      this.latitude = latitude;
   }

   public float getLongitude() {
      return longitude;
   }

   public void setLongitude(float longitude) {
      this.longitude = longitude;
   }

   public String getVisibility() {
      return visibility;
   }

   public void setVisibility(String visibility) {
      this.visibility = visibility;
   }

   @XmlElement(name = "battery_level")
   public String getBatteryLevel() {
      return batteryLevel;
   }

   public void setBatteryLevel(String batteryLevel) {
      this.batteryLevel = batteryLevel;
   }

   public String getHardware() {
      return hardware;
   }

   public void setHardware(String hardware) {
      this.hardware = hardware;
   }

   @Override
   public String toString() {
      return "Beacon{" +
         "id='" + id + '\'' +
         ", factoryID='" + factoryID + '\'' +
         ", iconURL='" + iconURL + '\'' +
         ", name='" + name + '\'' +
         ", latitude=" + latitude +
         ", longitude=" + longitude +
         ", visibility='" + visibility + '\'' +
         ", batteryLevel='" + batteryLevel + '\'' +
         ", hardware='" + hardware + '\'' +
         '}';
   }
}
