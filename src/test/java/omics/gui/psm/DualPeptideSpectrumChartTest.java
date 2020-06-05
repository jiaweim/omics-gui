package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 05 Jun 2020, 3:05 PM
 */
class DualPeptideSpectrumChartTest extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        DualPeptideSpectrumChart chart = new DualPeptideSpectrumChart();


        Scene scene = new Scene(chart);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}