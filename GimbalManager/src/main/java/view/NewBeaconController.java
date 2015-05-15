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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class NewBeaconController {
   @FXML
   private TextField factoryIDText;
   @FXML
   private Button okButton;
   @FXML
   private Button cancelButton;
   private Stage dialogStage;
   private boolean okClicked = false;
   private String factoryID;

   public String getFactoryID() {
      return factoryID;
   }

   @FXML
   public void handleCancelBtnClick(ActionEvent event) {
      dialogStage.close();
   }

   @FXML
   public void handleOkBtnClick(ActionEvent event) {
      factoryID = factoryIDText.getText();
      if (factoryID.indexOf('-') < 0) {
         factoryID = factoryID.substring(0, 4) + '-' + factoryID.substring(4, 9);
      }
      factoryID = factoryID.toUpperCase();
      okClicked = true;
      dialogStage.close();
   }

   public boolean isOkClicked() {
      return okClicked;
   }

   public void setDialogStage(Stage dialogStage) {
       this.dialogStage = dialogStage;
   }

   @FXML
   private void initialize() {
   }
}
