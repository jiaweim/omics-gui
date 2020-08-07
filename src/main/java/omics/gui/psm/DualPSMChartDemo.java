package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import omics.util.ms.peaklist.impl.DoublePeakList;

import java.util.Random;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 07 Aug 2020, 8:49 AM
 */
public class DualPSMChartDemo extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        StackPane root = new StackPane();
        DualPeptideSpectrumChart chart = new DualPeptideSpectrumChart();
        DoublePeakList list1 = new DoublePeakList();
        DoublePeakList list2 = new DoublePeakList();
        Random random = new Random();
        random.doubles(1000).forEach(value -> list1.add(value*1000, random.nextDouble()*1E4));
        random.doubles(1000).forEach(value -> list2.add(value*1000, random.nextDouble()*1E3));

        chart.setPeakList(list1, list2);

        root.getChildren().add(chart);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
