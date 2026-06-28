package com.example.elcincuentazo.controller;

import com.example.elcincuentazo.model.GameLogic;
import com.example.elcincuentazo.view.GameScreenView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de MainMenu.fxml.
 *
 * Recibe el modelo vía initModel() (llamado desde MainMenuView después
 * de cargar el FXML). Su única responsabilidad de lógica es:
 *   1. Habilitar "Comenzar partida" cuando el usuario elige rivales.
 *   2. Llamar model.setupGame(n) e iniciar GameScreenView.
 */
public class MainMenuController implements Initializable {

    // ── Header ───────────────────────────────────────────────────────────────
    @FXML private Label suitIconsTop;
    @FXML private Label gameTitle;
    @FXML private Label suitIconsBottom;
    @FXML private Label gameSubtitle;

    // ── Tarjeta central ───────────────────────────────────────────────────────
    @FXML private VBox menuCard;
    @FXML private Label sectionLabel;
    @FXML private HBox playerCountBox;

    // ── Selección de jugadores ────────────────────────────────────────────────
    @FXML private ToggleButton btn1Player;
    @FXML private ToggleButton btn2Players;
    @FXML private ToggleButton btn3Players;
    @FXML private ToggleGroup  playerCountGroup;   // definido con <fx:define> en el FXML

    // ── Reglas ────────────────────────────────────────────────────────────────
    @FXML private VBox  rulesSummary;
    @FXML private Label rulesTitle;

    // ── Botón iniciar ─────────────────────────────────────────────────────────
    @FXML private Button btnStartGame;

    // ── Footer ────────────────────────────────────────────────────────────────
    @FXML private Label cornerCardLeft;
    @FXML private Label footerText;
    @FXML private Label cornerCardRight;

    // ── Modelo ────────────────────────────────────────────────────────────────
    private GameLogic model;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ESTA ES LA FORMA INCONDICIONAL: Escucha directamente al grupo de botones
        playerCountGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            btnStartGame.setDisable(newVal == null);
        });
    }

    /**
     * Inyecta el modelo. Llamado por MainMenuView justo después de load().
     *
     * @param model instancia de GameLogic creada en main
     */
    public void initModel(GameLogic model) {
        this.model = model;
    }

    // ── Eventos FXML ──────────────────────────────────────────────────────────

    /** Llamado por los tres ToggleButtons. El listener ya gestiona btnStartGame. */
    @FXML
    private void onPlayerCountSelected(ActionEvent event) {
        //btnStartGame.setDisable(playerCountGroup.getSelectedToggle() == null);
    }

    /**
     * Llamado al pulsar "¡Comenzar partida!".
     * Configura el modelo con el número de rivales y abre GameScreenView.
     */
    @FXML
    private void onStartGame(ActionEvent event) {
        int numOpponents = getSelectedOpponentCount();

        // Configurar la lógica del juego con el número de rivales
        model.setupGame(numOpponents);

        try {
            // Cerrar el menú y abrir la pantalla de juego
            Stage menuStage = (Stage) btnStartGame.getScene().getWindow();
            menuStage.close();

            new GameScreenView(model, numOpponents);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private int getSelectedOpponentCount() {
        Toggle selected = playerCountGroup.getSelectedToggle();
        if (selected == btn1Player)  return 1;
        if (selected == btn2Players) return 2;
        if (selected == btn3Players) return 3;
        return 1;
    }
}
