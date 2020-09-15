package omics.gui;

import javafx.scene.control.Alert;

/**
 * Interface to show alert
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 03 Sep 2020, 4:58 PM
 */
public interface ShowAlert
{
    default void showAlert(Alert.AlertType type, String msg)
    {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }
}
