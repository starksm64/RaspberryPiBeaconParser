package view;

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

import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class RootController {

   @FXML
   private TabPane tabPane;
   @FXML
   private Tab beaconsTab;
   @FXML
   private Tab configurationTab;
   private IBeaconInfo beaconInfo;

   public IBeaconInfo getBeaconInfo() {
      return beaconInfo;
   }

   public void setBeaconInfo(IBeaconInfo beaconInfo) {
      this.beaconInfo = beaconInfo;
   }

   @FXML
   private void initialize() {

   }

   public TabPane getTabPane() {
      return tabPane;
   }
   public Tab getConfigurationTab() {
      return configurationTab;
   }

   public Tab getBeaconsTab() {
      return beaconsTab;
   }

   @FXML
   public void tabChanged(ObservableValue<? extends Tab> ov, Tab t0, Tab t1) {
       System.out.printf("Tab Selection tabChanged, ov=%s, t0=%s, t1=%s\n", ov, t0, t1);
   }

   @FXML
   public void tabChanged(Event event) {
      System.out.printf("Tab Selection tabChanged, event=%s\n", event);

   }
}
