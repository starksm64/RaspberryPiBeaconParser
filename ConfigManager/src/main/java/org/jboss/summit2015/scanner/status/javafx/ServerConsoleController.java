package org.jboss.summit2015.scanner.status.javafx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.StringWriter;

/**
 * Created by starksm on 6/18/15.
 */
public class ServerConsoleController {
    @FXML
    private TextArea consoleArea;
    @FXML
    private Button closeButton;

    @FXML
    private void handleCloseAction() {
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    public StringWriter getConsole() {
        return new StringWriter() {
            @Override
            public void write(String str) {
                Platform.runLater(() -> consoleArea.appendText(str));
            }
        };
    }
}
