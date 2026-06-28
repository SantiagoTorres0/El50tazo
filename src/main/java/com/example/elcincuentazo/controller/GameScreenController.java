package com.example.elcincuentazo.controller;

import com.example.elcincuentazo.exceptions.GameOverException;
import com.example.elcincuentazo.exceptions.InvalidCardPlayException;
import com.example.elcincuentazo.model.Card;
import com.example.elcincuentazo.model.CardValueCalculator;
import com.example.elcincuentazo.model.GameLogic;
import com.example.elcincuentazo.model.Player;
import com.example.elcincuentazo.observer.GameEventListener;
import com.example.elcincuentazo.view.MainMenuView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
 * de cargar el FXML). Crea internamente un {@link GameController} (clase
 * sin dependencias de JavaFX) que se encarga de orquestar {@code GameLogic}
 * y {@code TurnManager}; este controlador se limita a:
 *   (1) traducir clics de la GUI en llamadas a GameController,
 *   (2) traducir eventos del modelo (vía GameEventListener) en
 *       actualizaciones visuales.
 *
 * Toda la lógica de reglas vive en GameLogic / CardValueCalculator / Deck /
 * Player; toda la lógica de orquestación de turnos vive en GameController.
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

    // ── Controlador / Modelo ────────────────────────────────────────────────
    private GameController gameController;
    private GameLogic       model;       // acceso de lectura cómodo a gameController.getModel()
    private int             numOpponents;
    private Card             selectedCard;

    // ── Arreglos auxiliares para acceder a los paneles de CPU por índice ──────
    // Índice 0 = "Máquina 1" (model.getPlayers().get(1)), índice 1 = "Máquina 2", etc.
    private Pane[]  handPlayerPanes;
    private Label[] playerStatusLabels;
    private Label[] turnIndicatorLabels;
    private Label[] eliminatedBadgeLabels;

    /** Símbolos de palo indexados igual que Card.getSuit() (0=Picas, 1=Corazones, 2=Tréboles, 3=Diamantes). */
    private static final String[] SUIT_SYMBOLS = {"♠", "♥", "♣", "♦"};

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // gameController todavía es null aquí; la init real ocurre en initModel().
        // Los campos @FXML ya están inyectados, así que podemos armar los
        // arreglos auxiliares indexados por jugador-máquina (1, 2 o 3).
        handPlayerPanes       = new Pane[]{handPlayer1, handPlayer2, handPlayer3};
        playerStatusLabels    = new Label[]{lblPlayer1Status, lblPlayer2Status, lblPlayer3Status};
        turnIndicatorLabels   = new Label[]{indicatorPlayer1, indicatorPlayer2, indicatorPlayer3};
        eliminatedBadgeLabels = new Label[]{elimPlayer1, elimPlayer2, elimPlayer3};
    }

    /**
     * Inyecta el modelo. Llamado por GameScreenView justo después de load().
     * setupGame() ya fue ejecutado por MainMenuController antes de crear la vista.
     *
     * @param model        GameLogic ya configurado
     * @param numOpponents número de rivales (1, 2 o 3)
     */
    public void initModel(GameLogic model, int numOpponents) {
        this.gameController = new GameController(model);
        this.model           = gameController.getModel();
        this.numOpponents    = numOpponents;

        // Registrarse como observer del modelo (vía GameController)
        gameController.addGameEventListener(this);

        configureOpponentVisibility();
        synchronizeInitialState();

        // Si el primer turno fuera de una máquina, GameController la dispara sola.
        gameController.start();
    }

    // ── Configuración inicial ─────────────────────────────────────────────────

    private void configureOpponentVisibility() {
        boolean hasTwoOrMore = numOpponents >= 2;
        boolean hasThree     = numOpponents == 3;

        panePlayer2.setVisible(hasTwoOrMore);
        panePlayer2.setManaged(hasTwoOrMore);
        panePlayer3.setVisible(hasThree);
        panePlayer3.setManaged(hasThree);
    }

    private void synchronizeInitialState() {
        updateTableSum(model.getTableSum());
        updateDeckCounter(model.getDeck().drawPileSize());

        Card initialCard = model.getDeck().peekTableTop();
        if (initialCard != null) {
            lblNoCard.setVisible(false);
            lblLastCardDesc.setText(initialCard.toString());
        }

        // GameLogic.humanPlayCard(...) ya incluye el draw automático de la
        // carta de reemplazo, así que no existe una acción manual de "tomar
        // del mazo" en este diseño. Se deshabilitan los controles asociados.
        deckZone.setDisable(true);
        btnDrawCard.setDisable(true);

        renderHumanHand();
        for (int i = 1; i <= numOpponents; i++) {
            renderMachineHand(model.getPlayers().get(i));
        }

        // Reutilizamos onTurnStarted para fijar el estado visual del primer
        // turno (incluye la verificación de eliminación temprana).
        onTurnStarted(model.getCurrentPlayer());
    }

    // ── Renderizado de cartas ─────────────────────────────────────────────────

    private void renderHumanHand() {
        handHuman.getChildren().clear();
        selectedCard = null;
        btnPlayCard.setDisable(true);

        Player humanPlayer = model.getPlayers().get(0);
        for (Card card : humanPlayer.getHand().getCards()) {
            handHuman.getChildren().add(createHumanCardNode(card));
        }
    }

    private void renderMachineHand(Player machinePlayer) {
        int   playerIndex = model.getPlayers().indexOf(machinePlayer); // 1, 2 o 3
        Pane  handPane     = handPlayerPanes[playerIndex - 1];

        handPane.getChildren().clear();
        for (int i = 0; i < machinePlayer.getHand().size(); i++) {
            handPane.getChildren().add(createFaceDownCardNode());
        }
        playerStatusLabels[playerIndex - 1].setText(machinePlayer.getHand().size() + " cartas");
    }

    /** Crea el nodo visual de una carta boca arriba del jugador humano, seleccionable si es jugable. */
    private Node createHumanCardNode(Card card) {
        VBox cardNode = new VBox();
        cardNode.setAlignment(Pos.CENTER);
        cardNode.getStyleClass().addAll("card", isRedCard(card) ? "card-red" : "card-black");

        Label valueLabel = new Label(getValueLabel(card));
        valueLabel.getStyleClass().add("card-value-label");
        Label suitLabel = new Label(getSuitSymbol(card));
        suitLabel.getStyleClass().add("card-suit-label");
        cardNode.getChildren().addAll(valueLabel, suitLabel);

        boolean playable = CardValueCalculator.isPlayable(card, model.getTableSum());
        if (playable) {
            cardNode.getStyleClass().add("card-playable");
            cardNode.setOnMouseClicked(event -> selectCard(card, cardNode));
        } else {
            cardNode.setOpacity(0.45); // visualmente "no jugable" en este momento
        }

        return cardNode;
    }

    /** Crea el nodo visual de una carta boca abajo (mano de una máquina). */
    private Node createFaceDownCardNode() {
        VBox cardNode = new VBox();
        cardNode.getStyleClass().addAll("card", "card-small", "card-face-down");
        return cardNode;
    }

    private String getSuitSymbol(Card card) {
        int suit = card.getSuit();
        return (suit >= 0 && suit < SUIT_SYMBOLS.length) ? SUIT_SYMBOLS[suit] : "?";
    }

    private boolean isRedCard(Card card) {
        int suit = card.getSuit();
        return suit == 1 || suit == 3; // Corazones (1) y Diamantes (3)
    }

    private String getValueLabel(Card card) {
        switch (card.getValue()) {
            case 1:  return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(card.getValue());
        }
    }

    // ── Selección de carta ────────────────────────────────────────────────────

    private void selectCard(Card card, Node cardNode) {
        Player currentPlayer = model.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.isHuman()) {
            return; // no es el turno del humano, ignorar el clic
        }

        for (Node child : handHuman.getChildren()) {
            child.getStyleClass().remove("card-selected");
        }
        cardNode.getStyleClass().add("card-selected");

        selectedCard = card;
        btnPlayCard.setDisable(false);
        lblHumanStatus.setText("Carta seleccionada: " + card);
    }

    private void deselectCard() {
        for (Node child : handHuman.getChildren()) {
            child.getStyleClass().remove("card-selected");
        }
        selectedCard = null;
        btnPlayCard.setDisable(true);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private void updateTableSum(int sum) {
        lblTableSum.setText(String.valueOf(sum));
        double progress = Math.max(0, sum) / 50.0;
        pbSumProgress.setProgress(progress);
        pbSumProgress.getStyleClass().removeAll("sum-progress-warn", "sum-progress-danger");
        if      (sum > 45) pbSumProgress.getStyleClass().add("sum-progress-danger");
        else if (sum > 35) pbSumProgress.getStyleClass().add("sum-progress-warn");
    }

    private void updateDeckCounter(int remaining) {
        lblDeckCount.setText(String.valueOf(remaining));
        lblDeckCountSmall.setText(remaining + " restantes");
    }

    private void updateTurnIndicators(Player currentPlayer) {
        indicatorHuman.setVisible(currentPlayer.isHuman());
        for (Label indicator : turnIndicatorLabels) {
            indicator.setVisible(false);
        }
        if (!currentPlayer.isHuman()) {
            int playerIndex = model.getPlayers().indexOf(currentPlayer);
            turnIndicatorLabels[playerIndex - 1].setVisible(true);
        }
    }

    // ── Eventos FXML ──────────────────────────────────────────────────────────

    /** El jugador pulsa "Jugar carta ▶". */
    @FXML
    private void onPlayCard() {
        if (selectedCard == null) {
            return;
        }
        try {
            gameController.playCard(selectedCard);
            // El turno y la programación de la siguiente máquina (si aplica)
            // ya quedaron resueltos dentro de GameController.playCard(...).
        } catch (InvalidCardPlayException invalidPlay) {
            // onInvalidPlay() ya actualizó lblHumanStatus con el mensaje al
            // usuario; aquí solo limpiamos la selección visual.
            deselectCard();
        } catch (GameOverException gameOver) {
            lblGameMessage.setText(gameOver.getMessage());
        }
    }

    /** El jugador pulsa "Tomar del mazo" o hace clic en el mazo. */
    @FXML
    private void onDrawFromDeck() {
        // En El Cincuentazo el reemplazo de carta es automático: ocurre
        // dentro de GameController.playCard(...) justo después de jugar.
        // No existe (ni debe existir) una acción independiente de "tomar
        // carta", para no romper la regla de mantener siempre 4 cartas.
        lblHumanStatus.setText("Juega una carta: el reemplazo se toma automáticamente.");
    }

    /** Vuelve al menú principal. */
    @FXML
    private void onReturnToMenu() {
        if (gameController != null) {
            gameController.stop();
        }
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
        // Ya manejado en synchronizeInitialState()
    }

    @Override
    public void onTurnStarted(Player player) {
        // Caso borde descrito en el Javadoc de GameLogic: si llega el turno
        // a un humano sin ninguna carta jugable, debe quedar eliminado de
        // inmediato. checkAndEliminateCurrentPlayer() ya dispara los eventos
        // correspondientes (onPlayerEliminated / onGameOver) si corresponde.
        if (player.isHuman() && gameController.checkAndEliminateCurrentPlayer()) {
            return;
        }

        updateTurnIndicators(player);

        if (player.isHuman()) {
            renderHumanHand(); // refresca qué cartas son jugables con la suma actual
            lblTurnStatus.setText("Tu turno");
            lblHumanStatus.setText("Selecciona una carta para jugar");
        } else {
            lblTurnStatus.setText(player.getName() + " está jugando...");
            btnPlayCard.setDisable(true);
        }
    }

    @Override
    public void onCardPlayed(Player player, Card card, int newSum) {
        updateTableSum(newSum);
        lblLastCardDesc.setText(card.toString());
        lblNoCard.setVisible(false);
    }

    @Override
    public void onCardDrawn(Player player, Card card) {
        updateDeckCounter(model.getDeck().drawPileSize());
        if (player.isHuman()) {
            renderHumanHand();
        } else {
            renderMachineHand(player);
        }
    }

    @Override
    public void onDeckRefreshed(int newDeckSize) {
        updateDeckCounter(newDeckSize);
        lblGameMessage.setText("El mazo se ha barajado de nuevo.");
    }

    @Override
    public void onPlayerEliminated(Player player) {
        lblGameMessage.setText(player.getName() + " ha sido eliminado.");

        if (player.isHuman()) {
            indicatorHuman.setVisible(false);
            return;
        }

        int playerIndex = model.getPlayers().indexOf(player);
        turnIndicatorLabels[playerIndex - 1].setVisible(false);
        eliminatedBadgeLabels[playerIndex - 1].setVisible(true);
    }

    @Override
    public void onGameOver(Player winner) {
        String message = winner != null
                ? "¡" + winner.getName() + " gana la partida!"
                : "¡Empate, no hubo sobrevivientes!";

        lblGameMessage.setText(message);
        lblTurnStatus.setText("Partida finalizada");
        indicatorHuman.setVisible(false);
        btnPlayCard.setDisable(true);
        btnDrawCard.setDisable(true);

        if (gameController != null) {
            gameController.stop();
        }

        Alert gameOverAlert = new Alert(Alert.AlertType.INFORMATION);
        gameOverAlert.setTitle("Fin de la partida");
        gameOverAlert.setHeaderText(message);
        gameOverAlert.setContentText("Pulsa \"Menú\" para volver a jugar.");
        gameOverAlert.showAndWait();
    }

    @Override
    public void onInvalidPlay(Player player, Card card, int currentSum) {
        lblHumanStatus.setText("¡Esa carta supera 50! Elige otra.");
    }
}