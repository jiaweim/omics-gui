package omics.gui.controller;

import javafx.scene.control.Alert;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 31 Oct 2018, 9:48 AM
 */
public class ProcessorController
{
    void showAlter(Alert.AlertType alertType, String msg, String header)
    {
        Alert alert = new Alert(alertType);
        alert.setContentText(msg);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    void showAlter(Alert.AlertType alertType, String msg)
    {
        Alert alert = new Alert(alertType);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
