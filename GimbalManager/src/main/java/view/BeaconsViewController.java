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

import gimbal.Beacon;
import gimbal.BeaconConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BeaconModel;

import java.io.IOException;
import java.util.List;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconsViewController {
   @FXML
   private Button newBtn;
   @FXML
   private TextField filterField;
   @FXML
   private CheckBox minorIDBox;
   @FXML
   private CheckBox regexBox;
   @FXML
   private TableView<BeaconModel> beaconsTable;
   @FXML
   private TableColumn<BeaconModel, String> nameColumn;
   @FXML
   private TableColumn<BeaconModel, String> factoryIDColumn;
   @FXML
   private TableColumn<BeaconModel, Number> minorIDColumn;
   @FXML
   private TableColumn<BeaconModel, String> configNameColumn;

   private ObservableList<BeaconModel> beaconModels = FXCollections.observableArrayList();
   private IBeaconInfo beaconInfo;

   public IBeaconInfo getBeaconInfo() {
      return beaconInfo;
   }

   public void setBeaconInfo(IBeaconInfo beaconInfo) {
      this.beaconInfo = beaconInfo;
   }

   public List<BeaconModel> getBeaconModels() {
      return beaconModels;
   }

   @FXML
   private void handleRegexClick(ActionEvent event) {
      // If this is a regex
      boolean isRegex = regexBox.isSelected();

   }
   public void handleNewBtnClick(ActionEvent event) {
      String factoryID = showNewBeaconDialog();
      if(factoryID != null) {
         Beacon beacon = beaconInfo.registerBeacon(factoryID);
         if(beacon == null)
            return;
         BeaconConfiguration config = beaconInfo.getConfiguration(factoryID);
         BeaconModel bm = new BeaconModel(beacon, config);
         this.beaconModels.add(bm);
      }
   }

   public void setBeaconModels(List<Beacon> beacons) {
      this.beaconModels.clear();
      for(Beacon beacon : beacons) {
         BeaconConfiguration config = beaconInfo.getConfiguration(beacon.getFactoryID());
         BeaconModel bm = new BeaconModel(beacon, config);
         this.beaconModels.add(bm);
      }
   }
   /**
    * Initializes the controller class. This method is automatically called
    * after the fxml file has been loaded.
    * <p>
    * Initializes the table columns and sets up sorting and filtering.
    */
   @FXML
   private void initialize() {
      // 0. Initialize the columns.
      nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
      factoryIDColumn.setCellValueFactory(cellData -> cellData.getValue().factoryIDProperty());
      minorIDColumn.setCellValueFactory(cellData -> cellData.getValue().minorIDProperty());
      configNameColumn.setCellValueFactory(cellData -> cellData.getValue().configNameProperty());
      // 1. Wrap the ObservableList in a FilteredList (initially display all data).
      FilteredList<BeaconModel> filteredData = new FilteredList<>(beaconModels, p -> true);
      // 2. Set the filter Predicate whenever the filter changes.
      /*
      filterField.textProperty().addListener((observable, oldValue, newValue) -> {
         filteredData.setPredicate(beacon -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
               return true;
            }

            // Compare the beacon name and then factory id as partial strings by default
            String lowerCaseFilter = newValue.toLowerCase();

            if (beacon.getName().toLowerCase().contains(lowerCaseFilter)) {
               return true;
            } else if (beacon.getFactoryID().toLowerCase().contains(lowerCaseFilter)) {
               return true;
            }
            return false;
         });
      });
      */

      filterField.textProperty().addListener((observable, oldValue, newValue) -> {
         final boolean prioritizeMinorID = minorIDBox.isSelected();
         if(regexBox.isSelected())
            filteredData.setPredicate(new BeaconRegex(newValue, prioritizeMinorID));
         else
            filteredData.setPredicate(new BeaconPredicate(newValue, prioritizeMinorID));
         });

      // 3. Wrap the FilteredList in a SortedList.
      SortedList<BeaconModel> sortedData = new SortedList<>(filteredData);

      // 4. Bind the SortedList comparator to the TableView comparator.
      sortedData.comparatorProperty().bind(beaconsTable.comparatorProperty());

      // 5. Add sorted (and filtered) data to the table.
      beaconsTable.setItems(sortedData);
   }

   /**
    * Opens a dialog to get the factory id for the new beacon.
    *
    * @return true if the user clicked OK, false otherwise.
    */
   public String showNewBeaconDialog() {
      String factoryID = null;
     try {
       // Load the fxml file and create a new stage for the popup
       FXMLLoader loader = new FXMLLoader(BeaconsViewController.class.getResource("NewBeacon.fxml"));
       Pane page = loader.load();
       Stage dialogStage = new Stage();
       dialogStage.setTitle("Register Beacon...");
       dialogStage.initModality(Modality.WINDOW_MODAL);
       Scene scene = new Scene(page);
       dialogStage.setScene(scene);

       // Set the person into the controller
        NewBeaconController controller = loader.getController();
       controller.setDialogStage(dialogStage);

       // Show the dialog and wait until the user closes it
        dialogStage.showAndWait();
        if(controller.isOkClicked())
           factoryID = controller.getFactoryID();
     } catch (IOException e) {
       e.printStackTrace();
     }
      return factoryID;
   }
}
