package com.example.elcincuentazo.Model;

import javafx.application.Platform;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages turn progression and concurrency for the Cincuentazo game.
 *
 * <p>Two background threads are maintained:</p>
 * <ol>
 *   <li><b>Machine-thinking thread</b> – waits 2–4 seconds, then calls
 *       {@link GameLogic#executeMachinePlay()} to let the AI select and play a
 *       card.</li>
 *   <li><b>Machine-drawing thread</b> – waits an additional 1–2 seconds after
 *       the card is played before the turn is handed to the next player.  This
 *       delay is already embedded in the draw phase inside
 *       {@code executeMachinePlay()}; the scheduler simply chains the steps.</li>
 * </ol>
 *
 * <p>All callbacks into {@link GameLogic} that may touch JavaFX nodes are
 * dispatched via {@code Platform.runLater} so the JavaFX Application Thread
 * is never blocked.</p>
 *
 * <p>Thread safety: the executor is single-threaded, so only one machine
 * action runs at a time.  {@link GameLogic#executeMachinePlay()} is also
 * {@code synchronized}, providing a second layer of protection.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see GameLogic
 */
public class TurnManager {

    /** Logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(TurnManager.class.getName());

    // Timing constants (milliseconds)
    /** Minimum delay before a machine plays a card. */
    private static final int MACHINE_PLAY_MIN_MS  = 2000;
    /** Maximum delay before a machine plays a card. */
    private static final int MACHINE_PLAY_MAX_MS  = 4000;
    /** Minimum delay before a machine draws a replacement card. */
    private static final int MACHINE_DRAW_MIN_MS  = 1000;
    /** Maximum delay before a machine draws a replacement card. */
    private static final int MACHINE_DRAW_MAX_MS  = 2000;

    /** The game logic that TurnManager orchestrates. */
    private final GameLogic gameLogic;

    /** Random instance for computing delay durations. */
    private final Random random;

    /**
     * Single-threaded scheduled executor used for all machine-turn delays.
     * Using a single thread ensures machine actions are never concurrent.
     */
    private final ScheduledExecutorService scheduler;

    /** Reference to the most recently scheduled machine task (used to cancel it). */
    private ScheduledFuture<?> pendingTask;

    /** Whether the TurnManager is currently running. */
    private volatile boolean running;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a new {@code TurnManager} wired to the given game logic.
     *
     * @param gameLogic the game engine to drive; must not be {@code null}
     */
    public TurnManager(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.random    = new Random();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "CincuentazoMachineThread");
            t.setDaemon(true);   // won't prevent JVM shutdown
            return t;
        });
        this.running = false;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /**
     * Starts the TurnManager.
     *
     * <p>Call this once after {@link GameLogic#setupGame(int)} has finished.
     * The manager inspects the first player and, if it is a machine, schedules
     * the first machine turn automatically.</p>
     */
    public void start() {
        running = true;
        triggerNextTurnIfMachine();
    }

    /**
     * Stops the TurnManager and cancels any pending machine-turn tasks.
     * Should be called when the game ends or the application is closing.
     */
    public void stop() {
        running = false;
        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }
        scheduler.shutdownNow();
    }

    // -----------------------------------------------------------------------
    // Human-turn completion hook
    // -----------------------------------------------------------------------

    /**
     * Notifies the TurnManager that the human player has completed their turn.
     *
     * <p>The Controller must call this method after
     * {@link GameLogic#humanPlayCard(Card)} returns successfully.  The manager
     * then checks if the next player is a machine and, if so, schedules the
     * machine's delayed turn.</p>
     */
    public void onHumanTurnCompleted() {
        if (!running || gameLogic.isGameOver()) return;
        triggerNextTurnIfMachine();
    }

    // -----------------------------------------------------------------------
    // Internal scheduling
    // -----------------------------------------------------------------------

    /**
     * Inspects the current player and, if it is a machine, schedules the
     * machine's thinking delay followed by card play.
     *
     * <p>If the current player is human, nothing is scheduled (the human acts
     * via the GUI).</p>
     */
    private void triggerNextTurnIfMachine() {
        if (gameLogic.isGameOver()) return;

        Player current = gameLogic.getCurrentPlayer();
        if (current == null || current.isHuman()) return;

        // Schedule the machine's thinking delay
        long thinkDelayMs = randomBetween(MACHINE_PLAY_MIN_MS, MACHINE_PLAY_MAX_MS);
        LOGGER.info(current.getName() + " is thinking for " + thinkDelayMs + " ms");

        pendingTask = scheduler.schedule(
            this::executeMachineTurn,
            thinkDelayMs,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Runs on the machine thread: executes the machine's card play, then
     * schedules the drawing delay, then advances to the next turn.
     */
    private void executeMachineTurn() {
        if (!running || gameLogic.isGameOver()) return;

        // Phase 1 – play a card (dispatched to JavaFX thread for GUI safety)
        Platform.runLater(() -> {
            try {
                gameLogic.executeMachinePlay();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Machine play failed: " + e.getMessage(), e);
            }
        });

        // Phase 2 – after draw delay, chain to the next machine turn if needed
        long drawDelayMs = randomBetween(MACHINE_DRAW_MIN_MS, MACHINE_DRAW_MAX_MS);

        pendingTask = scheduler.schedule(() -> {
            if (!running || gameLogic.isGameOver()) return;
            // If after play+draw the next player is still a machine, schedule again
            Platform.runLater(this::triggerNextTurnIfMachine);
        }, drawDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a random integer between {@code min} (inclusive) and
     * {@code max} (inclusive).
     *
     * @param min minimum value
     * @param max maximum value
     * @return random value in [min, max]
     */
    private long randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
