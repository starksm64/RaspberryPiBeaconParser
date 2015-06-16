package org.jboss.summit2015.scanner.status.javafx;

import javafx.scene.Node;
import org.jboss.summit2015.scanner.status.model.ScannerInfo;

import java.util.Comparator;

/**
 * Created by starksm on 6/2/15.
 */
public class ScannerNode implements Comparator<ScannerNode> {
    private ScannerInfo info;
    private Node node;
    private ScannerInfoController controller;

    public ScannerNode(ScannerInfo info, Node node, ScannerInfoController controller) {
        this.info = info;
        this.node = node;
        this.controller = controller;
    }

    public ScannerInfo getInfo() {
        return info;
    }
    public void setInfo(ScannerInfo info) {
        this.info = info;
    }

    public Node getNode() {
        return node;
    }

    public ScannerInfoController getController() {
        return controller;
    }

    @Override
    public int compare(ScannerNode o1, ScannerNode o2) {
        return o1.info.compareTo(o2.info);
    }
}
