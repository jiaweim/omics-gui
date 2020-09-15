package omics.gui.glycan;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static omics.gui.glycan.SNFGShape.*;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 06 Jan 2020, 7:28 PM
 */
public class ShapeDemo extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        VBox root = new VBox(6);

        root.setPadding(new Insets(6));
        root.getChildren().add(SNFGShape.Hexose(25));
        root.getChildren().add(SNFGShape.Glc(25));
        root.getChildren().addAll(SNFGShape.GlcNAc, SNFGShape.GlcN(25));

        root.getChildren().add(new HBox(6, Hexuronate(25), GlcA(25), ManA, GalA, GulA, AltA, AllA, TalA, IdoA));
        root.getChildren().add(new HBox(6, Deoxyhexose, Qui, Rha, dGul6, dAlt6, dTal6, Fuc));
        root.getChildren().add(new HBox(6, DeoxyhexNAc, QuiNAc, RhaNAc, dAltNAc6, dTalNAc6, FucNAc));
        root.getChildren().add(new HBox(6, DiDeoxyhexose, Oli, Tyv, Abe, Par, Dig, Col));
        root.getChildren().add(new HBox(6, Pentose, Ara, Lyx, Xyl, Rib));
        root.getChildren().add(new HBox(6, Deoxynonulosonate, Kdn, Neu5Ac, Neu5Gc, Neu, Sia));
        root.getChildren().add(new HBox(6, DiDeoxynonulosonate, Pse, Leg, Aci, eLeg4));

        root.getChildren().add(new HBox(6, Api, Fru, Tag, Sor, Psi));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("SNFG Shapes");
        stage.show();
    }
}
