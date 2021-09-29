module omics.gui {
    requires omics.pdk;
    requires logback.core;

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.controlsfx.controls;

    exports omics.gui.psm;

    opens omics.gui to javafx.fxml, javafx.graphics;
    opens omics.gui.controller to javafx.fxml;
    opens omics.gui.control to javafx.fxml;
    opens omics.gui.setting to javafx.fxml;
    opens omics.gui.util to javafx.graphics;
    opens omics.gui.test to javafx.fxml, javafx.graphics, javafx.base;

}