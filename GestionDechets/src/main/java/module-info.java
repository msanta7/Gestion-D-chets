module com.example.gestiondechets {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.gestiondechets to javafx.fxml;
    exports com.example.gestiondechets;
}