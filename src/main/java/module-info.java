module com.example.elcincuentazo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.elcincuentazo to javafx.fxml;
    exports com.example.elcincuentazo.Model;
    exports com.example.elcincuentazo.Model.exceptions;
    exports com.example.elcincuentazo.Model.observer;
}