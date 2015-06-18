package org.jboss.summit2015.scanner.status.javafx;

import com.jcraft.jsch.JSchException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import org.infinispan.util.StringPropertyReplacer;
import org.jboss.summit2015.scanner.status.StatusProperties;
import org.jboss.summit2015.scanner.status.model.ScannerInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by starksm on 5/28/15.
 */
public class MainController {
    @FXML
    private MenuBar menuBar;
    @FXML
    private ListView<StatusProperty> propertyListView;
    @FXML
    private TextField lastUpdateField;
    @FXML
    private TextArea consoleArea;
    @FXML
    private GridPane gridPane;
    @FXML
    private TitledPane titledPane;
    @FXML
    private Button pingButton;
    private volatile ScannerInfo selectedInfo;
    private SimpleBooleanProperty noSelection = new SimpleBooleanProperty(true);
    private TreeMap<String, ScannerNode> scannersMap = new TreeMap<>();

    public ScannerNode addScanner(ScannerInfo info) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scannerInfo.fxml"));
        ScannerInfoController controller = new ScannerInfoController();
        controller.setScannerInfo(info, this);
        loader.setController(controller);
        ScannerNode scannerNode = null;
        try {
            Node scanner = loader.load();
            scannerNode = new ScannerNode(info, scanner, controller);
            scannersMap.put(info.getScannerID(), scannerNode);
            if(selectedInfo == null)
                selectedInfo = info;
            Platform.runLater(() -> updateGrid());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scannerNode;
    }
    public void updateScanner(ScannerInfo info) {
        ScannerNode scannerNode = scannersMap.get(info.getScannerID());
        if(scannerNode == null)
            scannerNode = addScanner(info);
        Platform.runLater(scannerNode.getController());
        if(selectedInfo != null && selectedInfo.equals(info)) {
            selectedInfo = info;
            Platform.runLater(this::updateSelectedScanner);
        }
        String msg = String.format("%s: %,d msgs", info.getScannerID(), info.getPublishCount());
        lastUpdateField.setText(msg);
    }

    public void selectScannerInfo(ScannerInfo si) {
        selectedInfo = si;
        noSelection.set(si == null);
        System.out.printf("selectScannerInfo, %s\n", si);
        // Title of list view is the scanner id
        titledPane.setText("Status for: "+si.getScannerID());
        Platform.runLater(this::updateSelectedScanner);
    }

    /**
     * Notification of scanners with more than 60 seconds without a heartbeat
     * @param scanners
     */
    public void missingHeartbeats(List<ScannerInfo> scanners) {
        // Just refresh the display of each scanner so its psuedo class is updated to the error state
        scanners.forEach((scanner) -> {
            ScannerNode scannerNode = scannersMap.get(scanner.getScannerID());
            Platform.runLater(scannerNode.getController());
        });
        // Always refresh the selected scanner status
        Platform.runLater(this::updateSelectedScanner);
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        propertyListView.setCellFactory((ListView<StatusProperty> list) -> new StatusPropertyCell());
        System.out.printf("MainController.initialize, titledPane=%s\n", titledPane);
        gridPane.heightProperty().addListener((observable, oldValue, newValue) -> System.out.printf("Height: %s\n", newValue));
        gridPane.widthProperty().addListener((observable, oldValue, newValue) -> System.out.printf("Height: %s\n", newValue));
        pingButton.disableProperty().bind(noSelection);
    }

    @FXML
    private void handlePingAction() {
        String hostIP = selectedInfo.getLastStatus().get(StatusProperties.HostIPAddress.name());
        consoleArea.setText(String.format("Pinging %s\n", hostIP));
        try {
            InetAddress hostAddr = InetAddress.getByName(hostIP);
            boolean reachable = hostAddr.isReachable(5000);
            consoleArea.setText(String.format("%s is %s", hostIP, reachable ? "reachable" : "NOT reachable"));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            consoleArea.setText(sw.toString());
        }
    }
    @FXML
    private void handleSSHAction() {

    }
    @FXML
    private void handleGitPullAction() {
        String hostIP = selectedInfo.getLastStatus().get(StatusProperties.HostIPAddress.name());
        StringProperty text = consoleArea.textProperty();
        consoleArea.setText(String.format("Beginning git update for: %s\n", selectedInfo.getScannerID()));
        Thread gitThread = new Thread(() -> doGitUpdate(hostIP));
        gitThread.start();
    }
    @FXML
    private void createSSHConfigAction() {
        consoleArea.setText(scannersMap.keySet().toString());
        scannersMap.forEach((name, node) -> {
            ScannerInfo info = node.getInfo();
            StringBuilder entry = new StringBuilder(String.format("\nHost %s\n", info.getScannerID().toLowerCase()));
            entry.append("HostName ");
            entry.append(info.getProperty(StatusProperties.HostIPAddress.name()));
            entry.append('\n');
            entry.append("IdentityFile ~/.ssh/scanners_root_rsa\n");
            entry.append("KeepAlive yes\n");
            entry.append("ServerAliveInterval 1\n");
            entry.append("User root\n\n");
            consoleArea.appendText(entry.toString());
        });
    }
    @FXML
    private void createScannerConfigAction() {
        consoleArea.setText("Creating scanners config...\n");
        StringBuilder config = new StringBuilder("\"scannerProperties\": [");
        scannersMap.forEach((name, node) -> {
            ScannerInfo info = node.getInfo();
            String entry = String.format("{\n" +
                "      \"scannerID\": \""+info.getScannerID()+"\",\n" +
                "      \"macaddr\": \""+info.getProperty(StatusProperties.MACAddress.name())+"\",\n" +
                "      \"hostname\": \"RaspberryPi2-Room201\",\n" +
                "      \"heartbeatUUID\": \"DAF246CEF20011E4B116123B93F75CBA\",\n" +
                "      \"destinationName\": \"beaconEvents\",\n" +
                "      \"useQueues\": \"false\",\n" +
                "      \"beaconID\": \"R4MQ-7RM41\",\n" +
                "      \"beaconNo\": \"3\"\n" +
                "    }", info.getScannerID().toLowerCase());
            /*
            entry.append("HostName ");
            entry.append(info.getProperty(StatusProperties.HostIPAddress.name()));
            entry.append('\n');
            entry.append("IdentityFile ~/.ssh/scanners_root_rsa\n");
            entry.append("KeepAlive yes\n");
            entry.append("ServerAliveInterval 1\n");
            entry.append("User root\n\n");
            */
            consoleArea.appendText(entry.toString());
        });
    }

    @FXML
    private void handleCloseAction() {
        System.exit(0);
    }

    private void doGitUpdate(String hostIP) {
        TextStream consoleStream = text -> Platform.runLater(() -> consoleArea.appendText(text));
        try {
            String info = JSchUtils.updateGitRepo(hostIP, consoleStream);
            consoleArea.appendText("Done");
        } catch (JSchException|IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            consoleArea.appendText(sw.toString());
        }
    }
    @FXML
    private void handleUpdateServiceAction() {

    }
    @FXML
    private void handleServiceStatusAction() {
        consoleArea.setText(String.format("Beginning service status check for: %s\n", selectedInfo.getScannerID()));
        String hostIP = selectedInfo.getLastStatus().get(StatusProperties.HostIPAddress.name());
        Thread actionThread = new Thread(() -> doServiceStatusAction(hostIP));
        actionThread.start();
    }
    private void doServiceStatusAction(String hostIP) {
        TextStream consoleStream = text -> Platform.runLater(() -> consoleArea.appendText(text));
        try {
            String info = JSchUtils.getServicesStatus(hostIP, consoleStream);
            consoleArea.appendText("Done");
        } catch (JSchException|IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            consoleArea.appendText(sw.toString());
        }
    }
    private void updateSelectedScanner() {
        if(selectedInfo == null)
            return;

        // Set select properties in display order
        ObservableList<StatusProperty> props = FXCollections.observableArrayList();
        StatusProperties[] displayProps = {
            StatusProperties.ScannerID,
            StatusProperties.HostIPAddress,
            StatusProperties.MACAddress,
            StatusProperties.SystemTime,
            StatusProperties.Uptime,
            StatusProperties.LoadAverage,
            StatusProperties.RawEventCount,
            StatusProperties.PublishEventCount,
            StatusProperties.HeartbeatCount,
            StatusProperties.HeartbeatRSSI,
            StatusProperties.MemTotal,
            StatusProperties.MemFree,
        };
        Map<String, String> statusProps = selectedInfo.getLastStatus();
        for(StatusProperties spe : displayProps) {
            try {
                String key = spe.name();
                String value = statusProps.get(key);
                StatusProperty sp = new StatusProperty(key, value);
                // Add twice, one for the property name, once for the value
                props.add(sp);
                props.add(sp);
            } catch (IllegalArgumentException e) {
            }
        }
        // Add a time since last heartbeat property after the scanner id
        String sinceLastHeartbeat = String.format("%,d seconds", selectedInfo.getSinceLastHeartbeat()/1000);
        System.out.printf("+++ %s, %s\n", selectedInfo.getScannerID(), sinceLastHeartbeat);
        StatusProperty sp = new StatusProperty("SinceLastHeartbeat", sinceLastHeartbeat);
        props.add(2, sp);
        props.add(3, sp);
        propertyListView.setItems(props);
    }
    private void updateGrid() {
        gridPane.getChildren().clear();
        int count = 0;
        for (String scannerID : scannersMap.keySet()) {
            int col = count % 4;
            int row = count / 4;
            ScannerNode scannerNode = scannersMap.get(scannerID);
            Node node = scannerNode.getNode();
            System.out.printf("add to grid:(%d,%d)\n", row, col);
            gridPane.add(node, col, row, 1, 1);
            gridPane.setHalignment(node, HPos.CENTER);
            count ++;
        }
        gridPane.setGridLinesVisible(true);
        gridPane.requestLayout();
    }
}
