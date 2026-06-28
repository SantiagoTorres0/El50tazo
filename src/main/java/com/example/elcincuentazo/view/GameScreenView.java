package com.example.elcincuentazo.view;

import com.example.elcincuentazo.controller.GameScreenController;
import com.example.elcincuentazo.model.GameLogic;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Vista de la pantalla de juego.
 *
 * Carga GameScreen.fxml, obtiene el controlador e inyecta el modelo
 * ya configurado (setupGame ya fue llamado en MainMenuController).
 * La transición desde el menú la dispara MainMenuController creando
 * un objeto de esta clase.
 */
public class GameScreenView extends Stage {

    /**
     * Carga el FXML del juego, inyecta el modelo y muestra la ventana.
     *
     * @param model        GameLogic ya inicializado con setupGame(n)
     * @param numOpponents número de rivales elegido (1, 2 o 3)
     * @throws IOException si el FXML no se encuentra o no puede cargarse
     */
    public GameScreenView(GameLogic model, int numOpponents) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            GameScreenView.class.getResource(
                "/com/example/elcincuentazo/GameScreen.fxml"
            )
        );
        Parent root = loader.load();

        // Inyectar el modelo en el controlador DESPUÉS de load()
        GameScreenController controller = loader.getController();
        controller.initModel(model, numOpponents);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            GameScreenView.class.getResource(
                "/com/example/elcincuentazo/styles.css"
            ).toExternalForm()
        );

        setTitle("El Cincuentazo — "
            + numOpponents + (numOpponents == 1 ? " rival" : " rivales"));
        setScene(scene);
        setResizable(true);
        show();
    }
}
