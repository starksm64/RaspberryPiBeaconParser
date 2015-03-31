package org.jboss.summit2015.config;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BaseConfig {
   private transient HashMap<String, Properties> propertiesByAddr;
   private String version;
   private String timestamp;
   private String comment;
   private String brokerURL;
   private String user;
   private String password;
   private String destinationName;
   private boolean useQueues;
   private int batchCount;
   private ArrayList<Properties> scannerProperties;

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public ZonedDateTime getTimestampDate() {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z yyyy");
      ZonedDateTime ldt = ZonedDateTime.parse(timestamp, formatter);
      return ldt;
   }

   public String getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
   }

   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public String getBrokerURL() {
      return brokerURL;
   }

   public void setBrokerURL(String brokerURL) {
      this.brokerURL = brokerURL;
   }

   public String getUser() {
      return user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public boolean isUseQueues() {
      return useQueues;
   }

   public void setUseQueues(boolean useQueues) {
      this.useQueues = useQueues;
   }

   public String getDestinationName() {
      return destinationName;
   }

   public void setDestinationName(String destinationName) {
      this.destinationName = destinationName;
   }

   public int getBatchCount() {
      return batchCount;
   }

   public void setBatchCount(int batchCount) {
      this.batchCount = batchCount;
   }

   public ArrayList<Properties> getScannerProperties() {
      return scannerProperties;
   }

   public void setScannerProperties(ArrayList<Properties> scannerProperties) {
      this.scannerProperties = scannerProperties;
   }

   @Override
   public String toString() {
      return "BaseConfig{" +
         "version='" + version + '\'' +
         ", timestamp='" + timestamp + '\'' +
         ", comment='" + comment + '\'' +
         ", brokerURL='" + brokerURL + '\'' +
         ", user='" + user + '\'' +
         ", password='" + password + '\'' +
         ", destinationName='" + destinationName + '\'' +
         ", useQueues=" + useQueues +
         ", batchCount=" + batchCount +
         ", scannerProperties=" + scannerProperties +
         '}';
   }

   public Properties findPropertiesByHardwareAddress(String key) {
      if(propertiesByAddr == null) {
         propertiesByAddr = new HashMap<>();
         for (Properties properties : scannerProperties) {
            String macaddr = properties.getProperty("macaddr");
            propertiesByAddr.put(macaddr, properties);
         }
      }
      return propertiesByAddr.get(key);
   }
}
