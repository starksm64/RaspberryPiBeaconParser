package org.jboss.summit2015.scanner.status.javafx;

import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jboss.summit2015.scanner.status.StatusProperties;
import org.jboss.summit2015.scanner.status.model.ScannerInfo;

import java.net.URL;

/**
 * Created by starksm on 5/28/15.
 */
public class ScannerInfoController implements Runnable {
    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    @FXML
    private ImageView image;
    @FXML
    private TitledPane titledPane;
    @FXML
    private Label beaconCount;
    private ScannerInfo scannerInfo;
    private MainController gridController;

    public void setScannerInfo(ScannerInfo scannerInfo, MainController gridController) {
        this.scannerInfo = scannerInfo;
        this.gridController = gridController;
    }

    @FXML
    private void initialize() {
        System.out.printf("ScannerInfoController.initialize\n");
        titledPane.textProperty().setValue(scannerInfo.getScannerID());
        scannerInfo.scannerIDProperty().bind(titledPane.textProperty());
        String systemType = scannerInfo.getLastStatus().get(StatusProperties.SystemType.name());
        if(systemType == null)
            systemType = "default";
        URL defaultURL = getClass().getResource("/images/"+systemType+".png");
        Image defaultImage = new Image(defaultURL.toExternalForm());
        image.setImage(defaultImage);
        image.setFitWidth(200);
        image.setFitHeight(150);
        image.setOnMouseClicked(me -> gridController.selectScannerInfo(scannerInfo));
    }

    /**
     * Called in a JavaFX thread to update the node view for the scanner
     */
    public void run() {
        String activeBeacons = scannerInfo.getLastStatus().get(StatusProperties.ActiveBeacons.name());
        beaconCount.textProperty().setValue(activeBeacons);
        System.out.printf("%s, elapsed=%d\n", scannerInfo.getScannerID(), scannerInfo.getSinceLastHeartbeat());
        if(scannerInfo.getSinceLastHeartbeat() > 60000)
            titledPane.pseudoClassStateChanged(errorClass, true);
        else
            titledPane.pseudoClassStateChanged(errorClass, false);
    }
}
