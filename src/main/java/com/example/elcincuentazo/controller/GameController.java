package com.example.elcincuentazo.controller;

import com.example.elcincuentazo.exceptions.GameOverException;
import com.example.elcincuentazo.exceptions.InvalidCardPlayException;
import com.example.elcincuentazo.model.Card;
import com.example.elcincuentazo.model.GameLogic;
import com.example.elcincuentazo.model.TurnManager;
import com.example.elcincuentazo.observer.GameEventListener;

/**
 * Controlador de juego independiente de JavaFX/FXML.
 *
 * <p>Esta clase es el punto único de entrada para las acciones que puede
 * disparar la GUI (jugar una carta, iniciar o detener la partida). No conoce
 * ningún nodo de JavaFX ni de Scene Builder: solo orquesta {@link GameLogic}
 * y {@link TurnManager}. Toda la lógica de reglas del juego permanece en el
 * modelo; este controlador únicamente delega y coordina el ciclo de vida del
 * turno, dejando a la View (por ejemplo {@code GameScreenController}) la
 * responsabilidad exclusiva de pintar la interfaz.</p>
 *
 * <p>La View se registra como {@link GameEventListener} a través de
 * {@link #addGameEventListener(GameEventListener)} y consulta el estado
 * actual mediante {@link #getModel()} para renderizar (cartas, suma, mazo,
 * jugadores, etc.).</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see GameLogic
 * @see TurnManager
 */
public class GameController {

    /** Motor de reglas del juego. */
    private final GameLogic model;

    /** Orquestador de turnos e hilos de las máquinas. */
    private final TurnManager turnManager;

    /**
     * Construye un {@code GameController} sobre un {@link GameLogic} ya
     * configurado (es decir, {@code setupGame(...)} ya fue llamado por
     * quien crea esta instancia, normalmente el controlador del menú).
     *
     * @param model instancia de {@link GameLogic} con la partida lista para jugar
     */
    public GameController(GameLogic model) {
        this.model       = model;
        this.turnManager = new TurnManager(model);
    }

    // -----------------------------------------------------------------------
    // Ciclo de vida de la partida
    // -----------------------------------------------------------------------

    /**
     * Inicia la partida. Si el primer turno corresponde a una máquina, el
     * {@link TurnManager} la dispara automáticamente tras su tiempo de
     * "pensado".
     */
    public void start() {
        turnManager.start();
    }

    /**
     * Detiene cualquier turno de máquina pendiente y cancela el scheduler
     * interno. Debe llamarse siempre que se abandona la pantalla de juego
     * (fin de partida o regreso al menú), para no dejar hilos colgados.
     */
    public void stop() {
        turnManager.stop();
    }

    // -----------------------------------------------------------------------
    // Acciones del jugador humano
    // -----------------------------------------------------------------------

    /**
     * Ejecuta la jugada de una carta por parte del humano y, si la jugada
     * fue válida, notifica al {@link TurnManager} para que programe a la
     * máquina siguiente en caso de que le corresponda el turno.
     *
     * @param card carta que el humano desea jugar
     * @throws InvalidCardPlayException si la carta haría exceder la suma de 50
     * @throws GameOverException        si la partida ya había terminado
     */
    public void playCard(Card card) throws InvalidCardPlayException, GameOverException {
        model.humanPlayCard(card);
        turnManager.onHumanTurnCompleted();
    }

    /**
     * Verifica si el jugador en turno debe ser eliminado por no tener
     * ninguna carta jugable y, de ser así, lo elimina. Pensado para
     * invocarse al iniciar el turno del jugador humano (caso borde
     * descrito en {@link GameLogic#checkAndEliminateCurrentPlayer()}).
     *
     * @return {@code true} si el jugador en turno fue eliminado
     */
    public boolean checkAndEliminateCurrentPlayer() {
        return model.checkAndEliminateCurrentPlayer();
    }

    // -----------------------------------------------------------------------
    // Registro de eventos y acceso de solo lectura al modelo
    // -----------------------------------------------------------------------

    /**
     * Registra un {@link GameEventListener} (normalmente la View) para
     * recibir los eventos de juego disparados por el modelo.
     *
     * @param listener el listener a registrar
     */
    public void addGameEventListener(GameEventListener listener) {
        model.addGameEventListener(listener);
    }

    /**
     * Devuelve el modelo de juego, para que la View pueda consultar el
     * estado actual (jugadores, mazo, suma, turno) al momento de renderizar.
     *
     * @return el {@link GameLogic} de esta partida
     */
    public GameLogic getModel() {
        return model;
    }
}