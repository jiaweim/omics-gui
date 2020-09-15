package omics.gui.glycan;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;

/**
 * Symbol nomenclature for glycans shapes.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 06 Jan 2020, 9:50 AM
 */
public class SNFGShape
{
    private static final double STROKE_WIDTH = 1.0;
    private static final double DEFAULT_SIZE = 21;

    public static final Color WHITE = Color.rgb(255, 255, 255);
    static final Color BLUE = Color.rgb(0, 144, 188);
    static final Color GREEN = Color.rgb(0, 166, 81);
    static final Color YELLOW = Color.rgb(255, 212, 0);
    static final Color LIGHT_BLUE = Color.rgb(143, 204, 233);
    static final Color PINK = Color.rgb(246, 158, 161);
    static final Color PURPLE = Color.rgb(165, 67, 153);
    static final Color BROWN = Color.rgb(161, 122, 77);
    public static final Color ORANGE = Color.rgb(244, 121, 32);
    static final Color RED = Color.rgb(237, 28, 36);

    private static Shape FilledCircle(double size, Color color)
    {
        Circle circle = new Circle(size / 2);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(STROKE_WIDTH);
        circle.setFill(color);
        return circle;
    }

    private static Shape FilledSquare(double size, Color color)
    {
        Rectangle rectangle = new Rectangle(size, size);
        rectangle.setStrokeWidth(STROKE_WIDTH);
        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(color);
        return rectangle;
    }

    private static Shape CrossedSquare(double size)
    {
        Path path = new Path(new MoveTo(0, 0),
                new HLineTo(size),
                new VLineTo(size),
                new HLineTo(0),
                new VLineTo(0),
                new LineTo(size, size));
        path.setStrokeWidth(1.0);

        return path;
    }

    private static Shape CrossedSquare(double size, Color color)
    {
        Path path = new Path(new MoveTo(0, 0),
                new HLineTo(size),
                new VLineTo(size),
                new HLineTo(0),
                new VLineTo(0),
                new LineTo(size, size));
        path.setStrokeWidth(1.0);
        LinearGradient gradient = new LinearGradient(1, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, color), new Stop(0.5, color), new Stop(0.5, Color.WHITE), new Stop(1, Color.WHITE));
        path.setFill(gradient);

        return path;
    }

    private static Shape DividedDiamond(double size)
    {
        Path path = new Path(new MoveTo(0, size / 2),
                new LineTo(size / 2, 0),
                new LineTo(size, size / 2),
                new LineTo(size / 2, size),
                new LineTo(0, size / 2),
                new LineTo(size, size / 2)
        );
        path.setStrokeWidth(STROKE_WIDTH);
        return path;
    }

    private static Shape DividedDiamond(double size, Color color, boolean isUp)
    {
        Path path = new Path(new MoveTo(0, size / 2),
                new LineTo(size / 2, 0),
                new LineTo(size, size / 2),
                new LineTo(size / 2, size),
                new LineTo(0, size / 2),
                new LineTo(size, size / 2)
        );
        path.setStrokeWidth(STROKE_WIDTH);
        LinearGradient gradient;
        if (isUp) {
            gradient = new LinearGradient(0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, color), new Stop(0.5, color), new Stop(0.5, WHITE), new Stop(1, WHITE));
        } else {
            gradient = new LinearGradient(0.5, 0, 0.5, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, WHITE), new Stop(0.5, WHITE), new Stop(0.5, color), new Stop(1, color));
        }
        path.setFill(gradient);
        return path;
    }

    private static Shape FilledTriangle(double size, Color color)
    {
        Path path = new Path(new MoveTo(size * .5, 0),
                new LineTo(size, size * 0.866),
                new HLineTo(0),
                new LineTo(size * .5, 0));
        path.setStrokeWidth(STROKE_WIDTH);
        path.setFill(color);
        return path;
    }

    private static Shape DividedTriangle(double size, Color color)
    {
        Path path = new Path(new MoveTo(size * .5, 0),
                new LineTo(size, size * 0.866),
                new HLineTo(0),
                new LineTo(size * .5, 0),
                new VLineTo(size * 0.866));
        path.setStrokeWidth(STROKE_WIDTH);
        LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, WHITE), new Stop(0.5, WHITE), new Stop(0.5, color), new Stop(1, color));
        path.setFill(gradient);
        return path;
    }

    private static Shape FlatRectangle(double size, Color color)
    {
        Rectangle rectangle = new Rectangle(0, size * 0.25, size, size * .5);
        rectangle.setFill(color);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(STROKE_WIDTH);
        return rectangle;
    }

    private static Shape FilledStar(double size, Color color)
    {
        Polygon polygon = new Polygon(size * 0.5, 0,
                size * 0.6236, size * 0.3739,
                size, size * 0.3819,
                size * 0.7, size * 0.6211,
                size * 0.8091, size,
                size * 0.5, size * 0.774,
                size * 0.1909, size,
                size * 0.3, size * 0.6211,
                0, size * 0.3819,
                size * 0.3764, size * 0.3739);
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setStroke(Color.BLACK);
        polygon.setFill(color);
        return polygon;
    }

    private static Shape FilledDiamond(double size, Color color)
    {
        Polygon polygon = new Polygon(size * .5, 0, size, size * .5, size * .5, size, 0, size * .5);
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setStroke(Color.BLACK);
        polygon.setFill(color);
        return polygon;
    }

    private static Shape FlatDiamond(double size, Color color)
    {
        Polygon polygon = new Polygon(0, size * .5,
                size * .5, size / 6,
                size, size * .5,
                size * .5, size * 5 / 6);
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setStroke(Color.BLACK);
        polygon.setFill(color);
        return polygon;
    }

    private static Shape FlatHexagon(double size, Color color)
    {
        Polygon polygon = new Polygon(0, size * .5,
                size * .2, size * .2,
                size * .8, size * .2,
                size, size * .5,
                size * .8, size * .8,
                size * .2, size * .8);
        polygon.setStroke(Color.BLACK);
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setFill(color);
        return polygon;
    }

    private static Shape Pentagon(double size, Color color)
    {
        Polygon polygon = new Polygon(
                size * .5, 0,
                size, size * 0.3632,
                size * 0.8091, size * 0.951,
                size * 0.1909, size * 0.951,
                0, size * 0.3632
        );
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setStroke(Color.BLACK);
        polygon.setFill(color);
        return polygon;
    }

    private static Polygon pentagon(double w, double h, Color color)
    {
        return pentagon(0, 0, w, h, color);
    }

    private static Polygon pentagon(double x, double y, double w, double h, Color color)
    {
        double rx = w / 2.;
        double ry = h / 2.;
        double cx = x + w / 2.;
        double cy = y + h / 2.;

        double step = Math.PI / 2.5;
        Polygon polygon = new Polygon();
        for (int i = 0; i <= 5; i++) {
            polygon.getPoints().addAll((int) cx + rx * Math.cos(i * step - Math.PI / 2),
                    (int) cy + ry * Math.sin(i * step - Math.PI / 2.0));
        }
        polygon.setFill(color);
        polygon.setStrokeWidth(STROKE_WIDTH);
        polygon.setStroke(Color.BLACK);
        return polygon;
    }

    public static Shape Api(double size)
    {
        return pentagon(size, size, BLUE);
    }

    public static Shape Api = Api(21);

    public static Shape Fru(double size)
    {
        return pentagon(size, size, GREEN);
    }

    public static Shape Fru = Fru(21);

    public static Shape Tag(double size)
    {
        return Pentagon(size, YELLOW);
    }

    public static Shape Tag = Tag(21);

    public static Shape Sor(double size)
    {
        return Pentagon(size, ORANGE);
    }

    public static Shape Sor = Sor(21);

    public static Shape Psi(double size)
    {
        return Pentagon(size, PINK);
    }

    public static Shape Psi = Psi(21);

    public static Shape Bac(double size)
    {
        return FlatHexagon(size, BLUE);
    }

    public static Shape Bac = Bac(21);

    public static Shape LDmanHep(double size)
    {
        return FlatHexagon(size, GREEN);
    }


    public static Shape DiDeoxynonulosonate(double size)
    {
        return FlatDiamond(size, WHITE);
    }

    public static Shape DiDeoxynonulosonate = DiDeoxynonulosonate(21);

    public static Shape Pse(double size)
    {
        return FlatDiamond(size, GREEN);
    }

    public static Shape Pse = Pse(21);

    public static Shape Leg(double size)
    {
        return FlatDiamond(size, YELLOW);
    }

    public static Shape Leg = Leg(21);

    public static Shape Aci(double size)
    {
        return FlatDiamond(size, PINK);
    }

    public static Shape Aci = Aci(21);

    public static Shape eLeg4(double size)
    {
        return FlatDiamond(size, LIGHT_BLUE);
    }

    public static Shape eLeg4 = eLeg4(21);

    public static Shape Deoxynonulosonate(double size)
    {
        return FilledDiamond(size, Color.WHITE);
    }

    public static Shape Deoxynonulosonate = Deoxynonulosonate(21);

    public static Shape Kdn(double size)
    {
        return FilledDiamond(size, GREEN);
    }

    public static Shape Kdn = Kdn(21);

    public static Shape Neu5Ac(double size)
    {
        return FilledDiamond(size, PURPLE);
    }

    public static Shape Neu5Ac = Neu5Ac(21);

    public static Shape Neu5Gc(double size)
    {
        return FilledDiamond(size, LIGHT_BLUE);
    }

    public static Shape Neu5Gc = Neu5Gc(21);

    public static Shape Neu(double size)
    {
        return FilledDiamond(size, BROWN);
    }

    public static Shape Neu = Neu(21);

    public static Shape Sia(double size)
    {
        return FilledDiamond(size, RED);
    }

    public static Shape Sia = Sia(21);

    public static Shape Pentose(double size)
    {
        return FilledStar(size, WHITE);
    }

    public static Shape Pentose = Pentose(21);

    public static Shape Ara(double size)
    {
        return FilledStar(size, GREEN);
    }

    public static Shape Ara = Ara(21);

    public static Shape Lyx(double size)
    {
        return FilledStar(size, YELLOW);
    }

    public static Shape Lyx = Lyx(21);

    public static Shape Xyl(double size)
    {
        return FilledStar(size, ORANGE);
    }

    public static Shape Xyl = Xyl(21);

    public static Shape Rib(double size)
    {
        return FilledStar(size, PINK);
    }

    public static Shape Rib = Rib(21);

    public static Shape DiDeoxyhexose(double size)
    {
        return FlatRectangle(size, WHITE);
    }

    public static Shape DiDeoxyhexose = DiDeoxyhexose(21);

    public static Shape Oli(double size)
    {
        return FlatRectangle(size, BLUE);
    }

    public static Shape Oli = Oli(21);

    public static Shape Tyv(double size)
    {
        return FlatRectangle(size, GREEN);
    }

    public static Shape Tyv = Tyv(21);

    public static Shape Abe(double size)
    {
        return FlatRectangle(size, ORANGE);
    }

    public static Shape Abe = Abe(DEFAULT_SIZE);

    public static Shape Par(double size)
    {
        return FlatRectangle(size, PINK);
    }

    public static Shape Par = Par(DEFAULT_SIZE);

    public static Shape Dig(double size)
    {
        return FlatRectangle(size, PURPLE);
    }

    public static Shape Dig = Dig(DEFAULT_SIZE);

    public static Shape Col(double size)
    {
        return FlatRectangle(size, LIGHT_BLUE);
    }

    public static Shape Col = Col(DEFAULT_SIZE);

    public static Shape DeoxyhexNAc(double size)
    {
        return DividedTriangle(size, WHITE);
    }

    public static Shape DeoxyhexNAc = DeoxyhexNAc(DEFAULT_SIZE);

    public static Shape QuiNAc(double size)
    {
        return DividedTriangle(size, BLUE);
    }

    public static Shape QuiNAc = QuiNAc(DEFAULT_SIZE);

    public static Shape RhaNAc(double size)
    {
        return DividedTriangle(size, GREEN);
    }

    public static Shape RhaNAc = RhaNAc(DEFAULT_SIZE);

    public static Shape dAltNAc6(double size)
    {
        return DividedTriangle(size, PINK);
    }

    public static Shape dAltNAc6 = dAltNAc6(DEFAULT_SIZE);

    public static Shape dTalNAc6(double size)
    {
        return DividedTriangle(size, LIGHT_BLUE);
    }

    public static Shape dTalNAc6 = dTalNAc6(DEFAULT_SIZE);

    public static Shape FucNAc(double size)
    {
        return DividedTriangle(size, RED);
    }

    public static Shape FucNAc = FucNAc(DEFAULT_SIZE);


    public static Shape Deoxyhexose(double size)
    {
        return FilledTriangle(size, WHITE);
    }

    public static Shape Deoxyhexose = Deoxyhexose(DEFAULT_SIZE);

    public static Shape Qui(double size)
    {
        return FilledTriangle(size, BLUE);
    }

    public static Shape Qui = FilledTriangle(21, BLUE);

    public static Shape Rha(double size)
    {
        return FilledTriangle(size, GREEN);
    }

    public static Shape Rha = FilledTriangle(DEFAULT_SIZE, GREEN);

    public static Shape dGul6(double size)
    {
        return FilledTriangle(size, ORANGE);
    }

    public static Shape dGul6 = FilledTriangle(DEFAULT_SIZE, ORANGE);

    public static Shape dAlt6(double size)
    {
        return FilledTriangle(size, PINK);
    }

    public static Shape dAlt6 = FilledTriangle(DEFAULT_SIZE, PINK);

    public static Shape dTal6(double size)
    {
        return FilledTriangle(size, LIGHT_BLUE);
    }

    public static Shape dTal6 = FilledTriangle(DEFAULT_SIZE, LIGHT_BLUE);

    public static Shape Fuc(double size)
    {
        return FilledTriangle(size, RED);
    }

    public static Shape Fuc = FilledTriangle(DEFAULT_SIZE, RED);

    public static Shape Hexuronate(double size)
    {
        return DividedDiamond(size);
    }

    public static Shape Hexuronate = Hexuronate(DEFAULT_SIZE);

    public static Shape GlcA(double size)
    {
        return DividedDiamond(size, BLUE, true);
    }

    public static Shape GlcA = GlcA(DEFAULT_SIZE);

    public static Shape ManA(double size)
    {
        return DividedDiamond(size, GREEN, true);
    }

    public static Shape ManA = ManA(DEFAULT_SIZE);

    public static Shape GalA(double size)
    {
        return DividedDiamond(size, YELLOW, true);
    }

    public static Shape GalA = GalA(DEFAULT_SIZE);

    public static Shape GulA(double size)
    {
        return DividedDiamond(size, ORANGE, true);
    }

    public static Shape GulA = GulA(DEFAULT_SIZE);

    public static Shape AltA(double size)
    {
        return DividedDiamond(size, PINK, false);
    }

    public static Shape AltA = AltA(DEFAULT_SIZE);

    public static Shape AllA(double size)
    {
        return DividedDiamond(size, PURPLE, true);
    }

    public static Shape AllA = AllA(DEFAULT_SIZE);

    public static Shape TalA(double size)
    {
        return DividedDiamond(size, LIGHT_BLUE, true);
    }

    public static Shape TalA = TalA(DEFAULT_SIZE);

    public static Shape IdoA(double size)
    {
        return DividedDiamond(size, BROWN, false);
    }

    public static Shape IdoA = IdoA(DEFAULT_SIZE);

    public static Shape GlcN(double size)
    {
        return CrossedSquare(size, BLUE);
    }

    public static Shape Hexose = Hexose(DEFAULT_SIZE);

    public static Shape Hexose(double size)
    {
        return FilledCircle(size, WHITE);
    }

    public static Shape Glc = Glc(DEFAULT_SIZE);

    public static Shape Glc(double size)
    {
        return FilledCircle(size, BLUE);
    }

    public static Shape Man = Man(DEFAULT_SIZE);

    public static Shape Man(double size)
    {
        return FilledCircle(size, GREEN);
    }

    public static Shape Gal = Gal(DEFAULT_SIZE);

    public static Shape Gal(double size)
    {
        return FilledCircle(size, YELLOW);
    }

    public static Shape Gul = Gul(DEFAULT_SIZE);

    public static Shape Gul(double size)
    {
        return FilledCircle(size, ORANGE);
    }

    public static Shape Alt = Alt(DEFAULT_SIZE);

    public static Shape Alt(double size)
    {
        return FilledCircle(size, PINK);
    }

    public static Shape All = All(DEFAULT_SIZE);

    public static Shape All(double size)
    {
        return FilledCircle(size, PURPLE);
    }

    public static Shape Tal = Tal(DEFAULT_SIZE);

    public static Shape Tal(double size)
    {
        return FilledCircle(size, LIGHT_BLUE);
    }

    public static Shape Ido = Ido(DEFAULT_SIZE);

    public static Shape Ido(double size)
    {
        return FilledCircle(size, BROWN);
    }

    public static Shape HexNAc = HexNAc(DEFAULT_SIZE);

    public static Shape HexNAc(double size)
    {
        return FilledSquare(size, WHITE);
    }

    public static Shape GlcNAc = GlcNAc(DEFAULT_SIZE);

    public static Shape GlcNAc(double size)
    {
        return FilledSquare(size, BLUE);
    }


}
