package com.example.elcincuentazo.view;

import com.example.elcincuentazo.controller.MainMenuController;
import com.example.elcincuentazo.model.GameLogic;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Vista del menú principal.
 *
 * Responsabilidad única: cargar MainMenu.fxml, obtener el controlador
 * e inyectarle el modelo. La lógica de negocio vive en GameLogic;
 * la lógica de interacción vive en MainMenuController.
 */
public class MainMenuView extends Stage {

    /**
     * Carga el FXML, inyecta el modelo en el controlador y muestra la ventana.
     *
     * @param model         instancia de GameLogic (modelo del juego)
     * @param primaryStage  Stage principal proporcionado por JavaFX
     * @throws IOException  si el FXML no se encuentra o no puede cargarse
     */
    public MainMenuView(GameLogic model, Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainMenuView.class.getResource(
                    "/com/example/elcincuentazo/MainMenu.fxml"
            )
        );
        Parent root = loader.load();

        // Inyectar el modelo en el controlador DESPUÉS de load()
        MainMenuController controller = loader.getController();
        controller.initModel(model);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
            MainMenuView.class.getResource(
                "/com/example/elcincuentazo/styles.css"
            ).toExternalForm()
        );

        primaryStage.setTitle("El Cincuentazo");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
