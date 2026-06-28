module com.example.elcincuentazo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.junit.jupiter.api;
    requires java.logging;
    requires org.junit.jupiter.params;


    opens com.example.elcincuentazo to javafx.fxml;
    opens com.example.elcincuentazo.view to javafx.fxml;
    opens com.example.elcincuentazo.controller to javafx.fxml;

    // Exporta los paquetes para que sean accesibles
    exports com.example.elcincuentazo;
    exports com.example.elcincuentazo.exceptions;
    exports com.example.elcincuentazo.observer;
    exports com.example.elcincuentazo.model;
    opens com.example.elcincuentazo.model to javafx.fxml;
}