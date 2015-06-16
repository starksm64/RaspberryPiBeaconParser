package org.jboss.summit2015.scanner.status.javafx;

import javafx.scene.control.ListCell;
import javafx.scene.layout.Border;

/**
 * Created by starksm on 5/28/15.
 */
public class StatusPropertyCell extends ListCell<StatusProperty> {
    @Override
    protected void updateItem(StatusProperty item, boolean empty) {
        super.updateItem(item, empty);
        if(empty)
            return;

        int index = super.getIndex();
        boolean isLabelCell = index % 2 == 0;
        String text = null;
        if(isLabelCell)
            text = item.getName();
        else
            text = item.getValue();
        super.setText(text);
    }
}
