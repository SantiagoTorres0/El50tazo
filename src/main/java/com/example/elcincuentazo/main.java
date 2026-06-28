package com.example.elcincuentazo;

import com.example.elcincuentazo.model.GameLogic;
import com.example.elcincuentazo.view.MainMenuView;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación El Cincuentazo.
 *
 * Sigue el patrón MVC del ejemplo:
 *   1. main crea el modelo (GameLogic)
 *   2. main crea la Vista del menú pasándole el modelo
 *   3. La Vista carga el FXML, obtiene el controlador e inyecta el modelo
 */
public class main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        GameLogic model = new GameLogic();
        new MainMenuView(model, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
