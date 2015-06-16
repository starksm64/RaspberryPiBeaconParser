package model;

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

import gimbal.BeaconConfiguration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ConfigModel {
   private BeaconConfiguration configuration;
   private IntegerProperty id;
   private StringProperty name;
   private StringProperty uuid;
   private IntegerProperty txpower;
   private IntegerProperty minorID;
   private IntegerProperty majorID;

   public ConfigModel() {

   }
   public ConfigModel(BeaconConfiguration configuration) {
      this.configuration = configuration;
      id = new SimpleIntegerProperty(configuration.getId());
      uuid = new SimpleStringProperty(configuration.getProximityUUID());
      name = new SimpleStringProperty(configuration.getName());
      txpower = new SimpleIntegerProperty(configuration.getTransmissionPowerAsInt());
      minorID = new SimpleIntegerProperty(configuration.getMinor());
      majorID = new SimpleIntegerProperty(configuration.getMajor());
   }

   public BeaconConfiguration getConfiguration() {
      return configuration;
   }

   public void setConfiguration(BeaconConfiguration configuration) {
      this.configuration = configuration;
   }

   public int getId() {
      return id.get();
   }

   public IntegerProperty idProperty() {
      return id;
   }

   public void setId(int id) {
      this.id.set(id);
   }

   public String getName() {
      return name.get();
   }

   public StringProperty nameProperty() {
      return name;
   }

   public void setName(String name) {
      this.name.set(name);
   }

   public String getUuid() {
      return uuid.get();
   }

   public StringProperty uuidProperty() {
      return uuid;
   }

   public void setUuid(String uuid) {
      this.uuid.set(uuid);
   }

   public int getTxpower() {
      return txpower.get();
   }

   public IntegerProperty txpowerProperty() {
      return txpower;
   }

   public void setTxpower(int txpower) {
      this.txpower.set(txpower);
   }

   public int getMinorID() {
      return minorID.get();
   }

   public IntegerProperty minorIDProperty() {
      return minorID;
   }

   public void setMinorID(int minorID) {
      this.minorID.set(minorID);
   }

   public int getMajorID() {
      return majorID.get();
   }

   public IntegerProperty majorIDProperty() {
      return majorID;
   }

   public void setMajorID(int majorID) {
      this.majorID.set(majorID);
   }

   public boolean idMatches(String fragment) {
      String testID = ""+getId();
      return testID.contains(fragment);
   }

   public boolean minorIDMatches(String fragment) {
      String testID = ""+getMinorID();
      return testID.contains(fragment);
   }

}
