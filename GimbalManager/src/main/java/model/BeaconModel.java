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

import gimbal.Beacon;
import gimbal.BeaconConfiguration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Wrapper class for the Beacon that exposes JavaFX properties
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconModel {
   private Beacon beacon;
   private BeaconConfiguration configuration;
   private StringProperty id;
   private StringProperty factoryID;
   private StringProperty name;
   private StringProperty batteryLevel;
   private IntegerProperty minorID;
   private StringProperty configName;

   public BeaconModel() {

   }
   public BeaconModel(Beacon beacon, BeaconConfiguration configuration) {
      this.beacon = beacon;
      this.configuration = configuration;
      id = new SimpleStringProperty(beacon.getId());
      factoryID = new SimpleStringProperty(beacon.getFactoryID());
      name = new SimpleStringProperty(beacon.getName());
      batteryLevel = new SimpleStringProperty(beacon.getBatteryLevel());
      minorID = new SimpleIntegerProperty(configuration.getMinor());
      configName = new SimpleStringProperty(configuration.getName());
   }

   public BeaconConfiguration getConfiguration() {
      return configuration;
   }

   public void setConfiguration(BeaconConfiguration configuration) {
      this.configuration = configuration;
   }

   public Beacon getBeacon() {
      return beacon;
   }

   public void setBeacon(Beacon beacon) {
      this.beacon = beacon;
   }

   public String getId() {
      return id.get();
   }

   public StringProperty idProperty() {
      return id;
   }

   public void setId(String id) {
      this.id.set(id);
   }

   public String getFactoryID() {
      return factoryID.get();
   }

   public StringProperty factoryIDProperty() {
      return factoryID;
   }

   public void setFactoryID(String factoryID) {
      this.factoryID.set(factoryID);
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

   public String getBatteryLevel() {
      return batteryLevel.get();
   }

   public StringProperty batteryLevelProperty() {
      return batteryLevel;
   }

   public void setBatteryLevel(String batteryLevel) {
      this.batteryLevel.set(batteryLevel);
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

   public String getConfigName() {
      return configName.get();
   }

   public StringProperty configNameProperty() {
      return configName;
   }

   public void setConfigName(String configName) {
      this.configName.set(configName);
   }
}
