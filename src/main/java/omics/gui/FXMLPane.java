package omics.gui;

import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 4:57 PM
 */
public interface FXMLPane
{
    default void load(ClassLoader classLoader, String fxmlPath)
    {
        URL fxmlUrl = classLoader.getResource(fxmlPath);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
