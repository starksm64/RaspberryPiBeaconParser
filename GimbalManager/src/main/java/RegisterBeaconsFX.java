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
import gimbal.BeaconConfigurationInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import view.BeaconsViewController;
import view.IBeaconInfo;
import view.RootController;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * javafx interface to interactively register and configure a beacon
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class RegisterBeaconsFX extends Application implements IBeaconInfo {
   private String token;
   private IGimbalManager manager;
   private String baseConfigName = "RedHatSummitBaseBeaconConfig";
   private Stage primaryStage;
   private BorderPane rootLayout;
   private List<Beacon> beacons;
   private HashMap<String, Beacon> beaconsMap = new HashMap<>();
   private HashMap<Integer, BeaconConfiguration> beaconConfigurations = new HashMap<>();
   private HashMap<String, BeaconConfiguration> beaconConfigByFactoryID = new HashMap<>();
   private HashMap<String, BeaconConfiguration> beaconConfigByName = new HashMap<>();
   private RootController rootController;
   private int nextMinorID = 170;

   @Override
   public void start(Stage primaryStage) throws Exception {
      this.primaryStage = primaryStage;
      token = ResteasyGimbalManager.loadManagerTokenFromLocalProperties();
      manager = ResteasyGimbalManager.getInstance(token);
      nextMinorID = manager.findAvailableMinorIDInRange(162, 200);
      System.out.printf("Selected nextMinorID=%d\n", nextMinorID);

      initRootLayout();

      showBeaconsOverview();
   }

   @Override
   public Beacon getBeacon(String factoryID) {
      return beaconsMap.get(factoryID);
   }

   @Override
   public BeaconConfiguration getConfiguration(String factoryID) {
      BeaconConfiguration config = beaconConfigByFactoryID.get(factoryID);
      if(config == null) {
         BeaconConfigurationInfo configInfo = manager.getBeaconConfigByFactoryID(factoryID);
         config = beaconConfigByName.get(configInfo.getAppliedConfiguration());
      }
      return config;
   }

   @Override
   public BeaconConfiguration getConfiguration(int id) {
      return beaconConfigurations.get(id);
   }

   @Override
   public Beacon registerBeacon(String factoryID) {
      Beacon beacon = null;
      int major = 0;
      int minor = nextMinorID;
      nextMinorID ++;
      String beaconName = "Beacon" + minor;
      try {
         BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName, factoryID, beaconName, major, minor);
         beacon = manager.getBeacon(factoryID);
         beaconConfigByFactoryID.put(factoryID, config);
         beaconConfigurations.put(config.getId(), config);
         beaconConfigByName.put(config.getName(), config);
         beacons.add(beacon);
         System.out.printf("+++ registerBeacon: %s\n\tconfig:%s\n", beacon, config);
      } catch (Exception e) {
         String msg = e.getMessage();
         Alert alert = new Alert(AlertType.ERROR, msg, ButtonType.OK);
         alert.showAndWait();
      }
      return beacon;
   }

   /**
    * Initializes the root layout.
    */
   public void initRootLayout() {
      try {
         // Load root layout from fxml file.
         FXMLLoader loader = new FXMLLoader();
         URL viewURL = RegisterBeaconsFX.class.getResource("view/RootLayout.fxml");
         loader.setLocation(viewURL);
         rootLayout = loader.load();
         rootController = loader.getController();
         rootController.setBeaconInfo(this);

         // Show the scene containing the root layout.
         Scene scene = new Scene(rootLayout);
         primaryStage.setScene(scene);
         primaryStage.show();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Shows the person overview inside the root layout.
    */
   public void showBeaconsOverview() {
      try {
         // Load the beacons
         beacons = manager.getBeacons();
         for(Beacon beacon : beacons) {
            beaconsMap.put(beacon.getFactoryID(), beacon);
         }
         // Load the configurations
         List<BeaconConfiguration> configs = manager.getBeaconConfigurations();
         for(BeaconConfiguration config : configs) {
            beaconConfigurations.put(config.getId(), config);
            beaconConfigByName.put(config.getName(), config);
            String name = config.getName();
            int dash = name.indexOf('-');
            if(dash > 0) {
               // Parse the factory id from the name: RedHatSummitBaseBeaconConfig-32J8-E29SP
               String factoryID = name.substring(dash+1);
               beaconConfigByFactoryID.put(factoryID, config);
            }
         }
         // Load person overview.
         FXMLLoader loader = new FXMLLoader();
         URL viewURL = RegisterBeaconsFX.class.getResource("view/BeaconsTable.fxml");
         loader.setLocation(viewURL);
         Pane beaconsView = loader.load();

         // Give the controller the beacons data
         BeaconsViewController controller = loader.getController();
         controller.setBeaconInfo(this);
         controller.setBeaconModels(beacons);

         // Set the view as the content of the beacons tab
         Tab beaconsTab = rootController.getBeaconsTab();
         beaconsTab.setContent(beaconsView);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void activateBeacon() {
      int major = 0;
      int minor = 1;
      String beaconName = "Beacon" + minor;
      String factoryID = "";
      System.out.printf("Activating(%s,%s)\n", factoryID, beaconName);
      BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName, factoryID, beaconName, major, minor);
      System.out.printf("Activated: %s, config=%s\n", beaconName, config);
   }

   /**
    * Add -Djava.util.logging.manager=org.jboss.logmanager.LogManager to enable logging
    *
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      launch(args);
   }
}
