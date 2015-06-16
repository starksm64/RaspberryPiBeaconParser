package org.jboss.summit2015.scanner.status.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jboss.summit2015.scanner.status.model.ScannerInfo;

import java.net.URL;
import java.util.Random;

public class MainFX extends Application {
    private ScannerHealthMonitor healthMonitor;

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxml = getClass().getResource("main.fxml");
        System.out.printf("Loading fxml: %s\n", fxml);
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        MainController controller = loader.getController();
        healthMonitor = new ScannerHealthMonitor();
        healthMonitor.connect(null, null);
        healthMonitor.setMainController(controller);
        primaryStage.setTitle("Beacon Scanners");
        primaryStage.setScene(new Scene(root, 1400, 1180));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
