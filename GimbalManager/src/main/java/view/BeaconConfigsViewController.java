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

import gimbal.BeaconConfiguration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.ConfigModel;

import java.util.Collection;
import java.util.List;

/**
 * Display the gimbal beacon configurations in a table view
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconConfigsViewController {
   @FXML
   private Button newBtn;
   @FXML
   private TextField filterField;
   @FXML
   private TableView<ConfigModel> configsTable;
   @FXML
   private TableColumn<ConfigModel, Number> idColumn;
   @FXML
   private TableColumn<ConfigModel, String> nameColumn;
   @FXML
   private TableColumn<ConfigModel, Number> txpowerColumn;
   @FXML
   private TableColumn<ConfigModel, Number> minorIDColumn;
   @FXML
   private TableColumn<ConfigModel, Number> majorIDColumn;
   @FXML
   private TableColumn<ConfigModel, String> uuidColumn;

   private ObservableList<ConfigModel> configModels = FXCollections.observableArrayList();
   private IBeaconInfo beaconInfo;

   public IBeaconInfo getBeaconInfo() {
      return beaconInfo;
   }

   public void setBeaconInfo(IBeaconInfo beaconInfo) {
      this.beaconInfo = beaconInfo;
   }

   public List<ConfigModel> getConfigModels() {
      return configModels;
   }
   public void setConfigModels(Collection<BeaconConfiguration> configs) {
      configModels.clear();
      for(BeaconConfiguration config : configs) {
         ConfigModel cm = new ConfigModel(config);
         configModels.add(cm);
      }
   }

   public void handleNewBtnClick(ActionEvent event) {
      System.out.printf("TODO, new configuration...\n");
   }

   @FXML
   private void initialize() {
      // 0. Initialize the columns.
      idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
      nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
      uuidColumn.setCellValueFactory(cellData -> cellData.getValue().uuidProperty());
      txpowerColumn.setCellValueFactory(cellData -> cellData.getValue().txpowerProperty());
      minorIDColumn.setCellValueFactory(cellData -> cellData.getValue().minorIDProperty());
      majorIDColumn.setCellValueFactory(cellData -> cellData.getValue().majorIDProperty());
      // 1. Wrap the ObservableList in a FilteredList (initially display all data).
      FilteredList<ConfigModel> filteredData = new FilteredList<>(configModels, p -> true);
      // 2. Set the filter Predicate whenever the filter changes.
      filterField.textProperty().addListener((observable, oldValue, newValue) -> {
         filteredData.setPredicate(beacon -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
               return true;
            }

            String lowerCaseFilter = newValue.toLowerCase();
            if (beacon.getName().toLowerCase().contains(lowerCaseFilter)) {
               return true;
            } else if (beacon.minorIDMatches(lowerCaseFilter)) {
               return true;
            } else if (beacon.idMatches(lowerCaseFilter)) {
               return true;
            }
            return false;
         });
      });

      // 3. Wrap the FilteredList in a SortedList.
      SortedList<ConfigModel> sortedData = new SortedList<>(filteredData);

      // 4. Bind the SortedList comparator to the TableView comparator.
      sortedData.comparatorProperty().bind(configsTable.comparatorProperty());

      // 5. Add sorted (and filtered) data to the table.
      configsTable.setItems(sortedData);
   }

}
