package omics.gui.control;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import omics.util.utils.StringUtils;
import omics.util.utils.SystemUtils;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 10 Sep 2020, 10:06 PM
 */
public class AboutWindow extends VBox
{
    public AboutWindow(String toolName, String toolVersion, String buildTime, String description)
    {
        setSpacing(10);
        setAlignment(Pos.TOP_LEFT);
        setPrefWidth(600);
        setPrefHeight(400);

        setPadding(new Insets(10));

        Label label = new Label(toolName + " " + toolVersion);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(label);

        getChildren().add(hBox);

        Font font = Font.font("Arial", 16);
        Label timeLabel = new Label("Built in " + buildTime);
        timeLabel.setFont(font);
        getChildren().add(timeLabel);

        if (StringUtils.isNotEmpty(description)) {
            Text text = new Text(description);
            text.setFont(font);
            text.setTextAlignment(TextAlignment.JUSTIFY);
            text.setWrappingWidth(480);

            getChildren().add(text);
        }

        // Empty line
        getChildren().add(new Label(""));
        Label runtimeLabel = new Label("Runtime version: " + SystemUtils.JAVA_RUNTIME_VERSION + " " + SystemUtils.OS_ARCH);
        runtimeLabel.setFont(font);
        getChildren().add(runtimeLabel);
        Label vmLabel = new Label("VM: " + SystemUtils.JAVA_VM_NAME + " by " + SystemUtils.JAVA_VM_VENDOR);
        vmLabel.setFont(font);
        getChildren().add(vmLabel);
        getChildren().add(new Label(""));

        Label mailLabel = new Label("Any questions about usage, please contact jiaweimao2019@outlook.com");
        mailLabel.setFont(font);
        getChildren().add(mailLabel);
    }
}
