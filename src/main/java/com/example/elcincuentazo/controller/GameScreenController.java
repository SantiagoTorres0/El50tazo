package com.example.elcincuentazo.controller;

import com.example.elcincuentazo.model.Card;
import com.example.elcincuentazo.model.GameLogic;
import com.example.elcincuentazo.model.Player;
import com.example.elcincuentazo.observer.GameEventListener;
import com.example.elcincuentazo.view.MainMenuView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de GameScreen.fxml.
 *
 * Recibe el modelo vía initModel() (llamado desde GameScreenView después
 * de cargar el FXML). Se registra como GameEventListener para que el
 * modelo le notifique los eventos de juego y actualice la UI.
 *
 * NOTA PARA EL COLEGA: los métodos onPlayCard, onDrawFromDeck y los
 * callbacks de GameEventListener tienen sus stubs listos. Solo hay que
 * completar los TODO con la lógica de presentación.
 */
public class GameScreenController implements Initializable, GameEventListener {

    // ── Top bar ───────────────────────────────────────────────────────────────
    @FXML private Label       lblDeckCount;       // nº cartas en el mazo
    @FXML private Label       lblTableSum;        // suma actual (ej. "17")
    @FXML private ProgressBar pbSumProgress;      // barra 0–50
    @FXML private Label       lblTurnStatus;      // "Tu turno" / "Máquina 1..."
    @FXML private Label       lblTurnIndicator;   // ● indicador de color

    // ── CPU 1 (siempre visible, arriba del centro) ────────────────────────────
    @FXML private VBox  panePlayer1;
    @FXML private Label lblPlayer1Name;
    @FXML private Label lblPlayer1Status;
    @FXML private Label indicatorPlayer1;
    @FXML private Label elimPlayer1;
    @FXML private HBox  handPlayer1;              // cartas boca abajo de CPU1

    // ── CPU 2 (columna izquierda, visible con ≥2 rivales) ────────────────────
    @FXML private VBox  panePlayer2;
    @FXML private Label lblPlayer2Name;
    @FXML private Label lblPlayer2Status;
    @FXML private Label indicatorPlayer2;
    @FXML private Label elimPlayer2;
    @FXML private VBox  handPlayer2;              // cartas boca abajo de CPU2

    // ── CPU 3 (columna derecha, visible solo con 3 rivales) ──────────────────
    @FXML private VBox  panePlayer3;
    @FXML private Label lblPlayer3Name;
    @FXML private Label lblPlayer3Status;
    @FXML private Label indicatorPlayer3;
    @FXML private Label elimPlayer3;
    @FXML private VBox  handPlayer3;              // cartas boca abajo de CPU3

    // ── Mesa central ──────────────────────────────────────────────────────────
    @FXML private StackPane deckZone;             // zona del mazo (clic = tomar)
    @FXML private Label     lblDeckCountSmall;    // "40 restantes"
    @FXML private StackPane discardZone;          // carta actual en mesa
    @FXML private Label     lblNoCard;            // "—" si mesa vacía
    @FXML private Label     lblLastCardDesc;      // descripción última carta
    @FXML private Label     lblArrow;             // "→"
    @FXML private Label     lblGameMessage;       // mensajes de estado

    // ── Jugador humano (bottom) ───────────────────────────────────────────────
    @FXML private HBox   handHuman;              // 4 cartas del jugador
    @FXML private Label  lblHumanStatus;         // instrucción al jugador
    @FXML private Label  indicatorHuman;         // "▶ Tu turno"
    @FXML private Button btnPlayCard;            // "Jugar carta ▶"
    @FXML private Button btnDrawCard;            // "Tomar del mazo"
    @FXML private Button btnMenuReturn;          // "Menú"

    // ── Modelo ────────────────────────────────────────────────────────────────
    private GameLogic model;
    private int       numOpponents;
    private Card      selectedCard;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // model todavía es null aquí; la init real ocurre en initModel()
    }

    /**
     * Inyecta el modelo. Llamado por GameScreenView justo después de load().
     * setupGame() ya fue ejecutado por MainMenuController antes de crear la vista.
     *
     * @param model        GameLogic ya configurado
     * @param numOpponents número de rivales (1, 2 o 3)
     */
    public void initModel(GameLogic model, int numOpponents) {
        this.model        = model;
        this.numOpponents = numOpponents;

        // Registrarse como observer del modelo
        model.addGameEventListener(this);

        configurarVisibilidadJugadores();
        sincronizarEstadoInicial();
    }

    // ── Configuración inicial ─────────────────────────────────────────────────

    private void configurarVisibilidadJugadores() {
        boolean hayDosOmas = numOpponents >= 2;
        boolean hayTres    = numOpponents == 3;

        panePlayer2.setVisible(hayDosOmas);
        panePlayer2.setManaged(hayDosOmas);
        panePlayer3.setVisible(hayTres);
        panePlayer3.setManaged(hayTres);
    }

    private void sincronizarEstadoInicial() {
        actualizarSumaMesa(model.getTableSum());
        actualizarContadorMazo(model.getDeck().drawPileSize());

        Card cartaInicial = model.getDeck().peekTableTop();
        if (cartaInicial != null) {
            lblNoCard.setVisible(false);
            lblLastCardDesc.setText(cartaInicial.toString());
        }

        lblTurnStatus.setText("Tu turno");
        lblHumanStatus.setText("Selecciona una carta para jugar");
        btnPlayCard.setDisable(true);
        btnDrawCard.setDisable(true);

        // TODO (colega): renderizar cartas del humano en handHuman
        // TODO (colega): renderizar cartas boca abajo de CPUs en handPlayer1/2/3
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private void actualizarSumaMesa(int suma) {
        lblTableSum.setText(String.valueOf(suma));
        double progreso = Math.max(0, suma) / 50.0;
        pbSumProgress.setProgress(progreso);
        pbSumProgress.getStyleClass().removeAll("sum-progress-warn", "sum-progress-danger");
        if      (suma > 45) pbSumProgress.getStyleClass().add("sum-progress-danger");
        else if (suma > 35) pbSumProgress.getStyleClass().add("sum-progress-warn");
    }

    private void actualizarContadorMazo(int restantes) {
        lblDeckCount.setText(String.valueOf(restantes));
        lblDeckCountSmall.setText(restantes + " restantes");
    }

    // ── Eventos FXML ──────────────────────────────────────────────────────────

    /** El jugador pulsa "Jugar carta ▶". */
    @FXML
    private void onPlayCard() {
        if (selectedCard == null) return;
        // TODO (colega): model.humanPlayCard(selectedCard)
        //                manejar InvalidCardPlayException y GameOverException
    }

    /** El jugador pulsa "Tomar del mazo" o hace clic en el mazo. */
    @FXML
    private void onDrawFromDeck() {
        // TODO (colega): model.getDeck().drawCard()
        //                añadir la carta a handHuman y actualizar contador
    }

    /** Vuelve al menú principal. */
    @FXML
    private void onReturnToMenu() {
        try {
            Stage gameStage = (Stage) btnMenuReturn.getScene().getWindow();
            gameStage.close();

            GameLogic freshModel = new GameLogic();
            new MainMenuView(freshModel, new Stage());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── GameEventListener ─────────────────────────────────────────────────────

    @Override
    public void onGameStarted(int tableSum) {
        // Ya manejado en sincronizarEstadoInicial()
    }

    @Override
    public void onTurnStarted(Player player) {
        if (player.isHuman()) {
            lblTurnStatus.setText("Tu turno");
            indicatorHuman.setVisible(true);
            indicatorPlayer1.setVisible(false);
            indicatorPlayer2.setVisible(false);
            indicatorPlayer3.setVisible(false);
            btnPlayCard.setDisable(false);
        } else {
            lblTurnStatus.setText(player.getName() + " está jugando...");
            indicatorHuman.setVisible(false);
            btnPlayCard.setDisable(true);
            // TODO (colega): mostrar indicador en el panel CPU correcto
        }
    }

    @Override
    public void onCardPlayed(Player player, Card card, int newSum) {
        actualizarSumaMesa(newSum);
        lblLastCardDesc.setText(card.toString());
        lblNoCard.setVisible(false);
        // TODO (colega): animar carta en discardZone
    }

    @Override
    public void onCardDrawn(Player player, Card card) {
        actualizarContadorMazo(model.getDeck().drawPileSize());
        // TODO (colega): actualizar la mano del jugador correspondiente en la UI
    }

    @Override
    public void onDeckRefreshed(int newDeckSize) {
        actualizarContadorMazo(newDeckSize);
        lblGameMessage.setText("El mazo se ha barajado de nuevo.");
    }

    @Override
    public void onPlayerEliminated(Player player) {
        lblGameMessage.setText(player.getName() + " ha sido eliminado.");
        // TODO (colega): mostrar badge elimPlayer1/2/3 según el jugador
    }

    @Override
    public void onGameOver(Player winner) {
        String msg = winner != null
            ? "¡" + winner.getName() + " gana la partida!"
            : "¡Empate!";
        lblGameMessage.setText(msg);
        btnPlayCard.setDisable(true);
        btnDrawCard.setDisable(true);
        // TODO (colega): mostrar overlay de fin de juego
    }

    @Override
    public void onInvalidPlay(Player player, Card card, int currentSum) {
        lblHumanStatus.setText("¡Esa carta supera 50! Elige otra.");
    }
}
