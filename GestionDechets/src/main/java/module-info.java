module com.example.gestiondechets {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires kernel;
    requires layout;
    requires io;
    requires org.apache.pdfbox;


    opens com.example.gestiondechets to javafx.fxml;
    exports com.example.gestiondechets;
}