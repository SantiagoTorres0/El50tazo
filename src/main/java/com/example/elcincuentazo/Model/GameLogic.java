package com.example.elcincuentazo.Model;

import com.example.elcincuentazo.Model.exceptions.GameOverException;
import com.example.elcincuentazo.Model.exceptions.InvalidCardPlayException;
import com.example.elcincuentazo.Model.observer.GameEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central game-logic engine for Cincuentazo.
 *
 * <p>This class orchestrates every phase of the game:</p>
 * <ol>
 *   <li>Setup ({@link #setupGame(int)}) – creates the deck, creates all players,
 *       deals 4 cards each, places the initial table card.</li>
 *   <li>Turn execution ({@link #humanPlayCard(Card)}) – validates and executes
 *       a card play by the human player, then draws a replacement card.</li>
 *   <li>Elimination checking ({@link #checkAndEliminateCurrentPlayer()}) –
 *       detects when a player has no legal move.</li>
 *   <li>Win detection ({@link #isGameOver()}) – declares a winner when only one
 *       player remains active.</li>
 * </ol>
 *
 * <p>Machine turns are <em>initiated</em> by {@link TurnManager}, which calls
 * {@link #executeMachinePlay()} from a background thread after an appropriate
 * delay.</p>
 *
 * <p>The game fires events to registered {@link GameEventListener}s so that the
 * View can update the GUI without coupling to the Model.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see Deck
 * @see Player
 * @see TurnManager
 */
public class GameLogic {

    // -----------------------------------------------------------------------
    // State fields
    // -----------------------------------------------------------------------

    /** The deck (draw pile + table pile). */
    private Deck deck;

    /**
     * All players in turn order.  Index 0 is always the human player;
     * indices 1-N are machine players.
     */
    private List<Player> players;

    /** Index into {@code players} pointing to the active turn. */
    private int currentPlayerIndex;

    /** Accumulated sum on the table (mesa). */
    private int tableSum;

    /** Whether the game has finished. */
    private boolean gameOver;

    /** The winning player once the game ends; {@code null} until then. */
    private Player winner;

    /** Observers notified on game events (View / Controller layer). */
    private final List<GameEventListener> listeners;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a new {@code GameLogic} instance.
     * Call {@link #setupGame(int)} before starting to play.
     */
    public GameLogic() {
        this.listeners = new ArrayList<>();
        this.gameOver  = false;
    }

    // -----------------------------------------------------------------------
    // Listener registration
    // -----------------------------------------------------------------------

    /**
     * Registers a {@link GameEventListener} to receive game-event callbacks.
     *
     * @param listener the listener to add; must not be {@code null}
     */
    public void addGameEventListener(GameEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered {@link GameEventListener}.
     *
     * @param listener the listener to remove
     */
    public void removeGameEventListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    /**
     * Sets up a new game with the given number of machine opponents.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Creates a fresh, shuffled {@link Deck}.</li>
     *   <li>Creates one {@link HumanPlayer} and {@code machineCount}
     *       {@link MachinePlayer}s.</li>
     *   <li>Deals 4 cards to each player from the draw pile.</li>
     *   <li>Places one card face-up on the table to start the sum.</li>
     *   <li>Fires {@code onGameStarted} on all listeners.</li>
     * </ol>
     *
     * @param machineCount number of machine players to create (1, 2, or 3)
     * @throws IllegalArgumentException if {@code machineCount} is not 1, 2, or 3
     */
    public void setupGame(int machineCount) {
        if (machineCount < 1 || machineCount > 3) {
            throw new IllegalArgumentException(
                "Machine count must be 1, 2 or 3. Got: " + machineCount
            );
        }

        this.deck     = new Deck();
        this.players  = new ArrayList<>();
        this.tableSum = 0;
        this.gameOver = false;
        this.winner   = null;
        this.currentPlayerIndex = 0;

        // Create players – human always first
        players.add(new HumanPlayer());
        for (int i = 1; i <= machineCount; i++) {
            players.add(new MachinePlayer(i));
        }

        // Deal 4 cards to each player
        for (Player player : players) {
            for (int i = 0; i < PlayerHand.MAX_CARDS; i++) {
                Card card = deck.drawCard();
                player.receiveCard(card);
            }
        }

        // Place the initial card on the table
        Card initialCard = deck.drawCard();
        deck.placeOnTable(initialCard);
        tableSum = CardValueCalculator.calculateDelta(initialCard, 0);

        fireGameStarted(tableSum);
    }

    // -----------------------------------------------------------------------
    // Human player actions (called by Controller on JavaFX thread)
    // -----------------------------------------------------------------------

    /**
     * Executes a card play by the human player.
     *
     * <p>The Controller calls this method in response to the player clicking
     * a card in the GUI.  The method:</p>
     * <ol>
     *   <li>Validates the play (sum must not exceed 50).</li>
     *   <li>Removes the card from the human's hand and places it on the table.</li>
     *   <li>Updates the table sum.</li>
     *   <li>Draws a replacement card from the deck.</li>
     *   <li>Advances the turn to the next active player.</li>
     * </ol>
     *
     * @param card the card the human wishes to play
     * @throws InvalidCardPlayException if the card would push the sum above 50
     * @throws GameOverException        if the game has already ended
     * @throws IllegalStateException    if it is not currently the human's turn
     */
    public void humanPlayCard(Card card)
            throws InvalidCardPlayException, GameOverException {

        ensureGameNotOver();

        Player current = players.get(currentPlayerIndex);
        if (!current.isHuman()) {
            throw new IllegalStateException("It is not the human player's turn.");
        }

        int delta = CardValueCalculator.calculateDelta(card, tableSum);
        if (tableSum + delta > 50) {
            fireInvalidPlay(current, card, tableSum);
            throw new InvalidCardPlayException(
                "Cannot play " + card + ": sum would be " + (tableSum + delta),
                tableSum, delta
            );
        }

        // Execute the play
        current.playCard(card, tableSum);
        deck.placeOnTable(card);
        tableSum += delta;
        fireCardPlayed(current, card, tableSum);

        // Draw a replacement card
        Card drawn = deck.drawCard();
        current.receiveCard(drawn);
        fireCardDrawn(current, drawn);

        // Move to the next player
        advanceTurn();
    }

    // -----------------------------------------------------------------------
    // Machine player actions (called by TurnManager from background thread)
    // -----------------------------------------------------------------------

    /**
     * Executes the current machine player's full turn atomically.
     *
     * <p>{@link TurnManager} calls this method after the appropriate
     * "thinking" delay.  The method:</p>
     * <ol>
     *   <li>Verifies it is actually a machine's turn.</li>
     *   <li>Asks the machine to choose the best playable card.</li>
     *   <li>If no playable card exists, eliminates the machine.</li>
     *   <li>Otherwise plays the chosen card, updates the sum, draws a
     *       replacement, and advances the turn.</li>
     * </ol>
     *
     * <p>All listener callbacks issued here will be called on whatever thread
     * this method runs on.  {@link TurnManager} is responsible for
     * dispatching those calls back to the JavaFX Application Thread via
     * {@code Platform.runLater} before calling this method.</p>
     *
     * @throws GameOverException     if the game has already ended
     * @throws IllegalStateException if the current player is actually human
     */
    public synchronized void executeMachinePlay() throws GameOverException {
        ensureGameNotOver();

        Player current = players.get(currentPlayerIndex);
        if (current.isHuman()) {
            throw new IllegalStateException("It is not a machine player's turn.");
        }

        MachinePlayer machine = (MachinePlayer) current;

        // Check if the machine has any playable card
        if (!machine.hasPlayableCard(tableSum)) {
            eliminatePlayer(machine);
            return;
        }

        // Choose and execute the best card
        Card chosen = machine.chooseBestCard(tableSum);
        int delta = CardValueCalculator.calculateDelta(chosen, tableSum);

        try {
            machine.playCard(chosen, tableSum);
        } catch (InvalidCardPlayException e) {
            // Should never happen because chooseBestCard already validated;
            // treat as elimination defensively.
            eliminatePlayer(machine);
            return;
        }

        deck.placeOnTable(chosen);
        tableSum += delta;
        fireCardPlayed(machine, chosen, tableSum);

        // Draw replacement
        Card drawn = deck.drawCard();
        machine.receiveCard(drawn);
        fireCardDrawn(machine, drawn);

        advanceTurn();
    }

    // -----------------------------------------------------------------------
    // Elimination / win checks
    // -----------------------------------------------------------------------

    /**
     * Checks whether the current player must be eliminated (no playable card)
     * and, if so, triggers elimination.
     *
     * <p>The Controller should call this at the start of each human turn to
     * detect an edge case where the human cannot play any card after an
     * opponent has raised the sum.</p>
     *
     * @return {@code true} if the current player was eliminated
     */
    public boolean checkAndEliminateCurrentPlayer() {
        Player current = players.get(currentPlayerIndex);
        if (!current.hasPlayableCard(tableSum)) {
            eliminatePlayer(current);
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the game has ended (only one active player
     * remains or the game-over flag has been set).
     *
     * @return {@code true} when the game is over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the winning player, or {@code null} if the game is not yet over.
     *
     * @return winner or {@code null}
     */
    public Player getWinner() {
        return winner;
    }

    // -----------------------------------------------------------------------
    // State accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the current accumulated sum on the table.
     *
     * @return table sum
     */
    public int getTableSum() {
        return tableSum;
    }

    /**
     * Returns the player who is currently taking their turn.
     *
     * @return current {@link Player}, or {@code null} if game is over
     */
    public Player getCurrentPlayer() {
        if (gameOver) return null;
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns an unmodifiable view of all players (active and eliminated),
     * in the turn order established at setup.
     *
     * @return player list
     */
    public List<Player> getPlayers() {
        return java.util.Collections.unmodifiableList(players);
    }

    /**
     * Returns only the players who are still active.
     *
     * @return list of active players
     */
    public List<Player> getActivePlayers() {
        return players.stream()
                      .filter(Player::isActive)
                      .collect(Collectors.toList());
    }

    /**
     * Returns a snapshot of the current game state suitable for passing to
     * the View layer without exposing mutable internals.
     *
     * @return current {@link GameState} snapshot
     */
    public GameState getGameState() {
        return new GameState(
            players,
            deck.peekTableTop(),
            tableSum,
            deck.drawPileSize(),
            gameOver ? null : players.get(currentPlayerIndex),
            winner,
            gameOver
        );
    }

    /**
     * Returns the {@link Deck} used in this game.
     * Exposed for unit testing; the View and Controller should not modify it
     * directly.
     *
     * @return the game's deck
     */
    public Deck getDeck() {
        return deck;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Advances the turn to the next active player, skipping eliminated ones.
     * If only one player remains active, the game ends.
     */
    private void advanceTurn() {
        List<Player> active = getActivePlayers();
        if (active.size() <= 1) {
            declareWinner(active.isEmpty() ? null : active.get(0));
            return;
        }

        // Find next active player in circular order
        int total = players.size();
        int next  = (currentPlayerIndex + 1) % total;
        while (!players.get(next).isActive()) {
            next = (next + 1) % total;
        }
        currentPlayerIndex = next;
        fireTurnStarted(players.get(currentPlayerIndex));
    }

    /**
     * Eliminates a player: marks them inactive, returns their cards to the
     * bottom of the draw pile, fires the elimination event, then checks for
     * a winner.
     *
     * @param player the player to eliminate
     */
    private void eliminatePlayer(Player player) {
        // Return cards to bottom of deck
        List<Card> returnedCards = new ArrayList<>(player.getHand().getCards());
        for (Card card : returnedCards) {
            player.getHand().removeCard(card);
        }
        deck.returnCardsToBottom(returnedCards);

        player.eliminate();
        firePlayerEliminated(player);

        // Check if game should end
        List<Player> active = getActivePlayers();
        if (active.size() <= 1) {
            declareWinner(active.isEmpty() ? null : active.get(0));
        } else {
            // Advance to next active player
            advanceTurn();
        }
    }

    /**
     * Ends the game and announces the winner.
     *
     * @param winningPlayer the surviving player (may be {@code null} in the
     *                      extremely rare case of simultaneous elimination)
     */
    private void declareWinner(Player winningPlayer) {
        this.gameOver = true;
        this.winner   = winningPlayer;
        fireGameOver(winningPlayer);
    }

    /**
     * Throws {@link GameOverException} if the game has already ended.
     *
     * @throws GameOverException if game is over
     */
    private void ensureGameNotOver() throws GameOverException {
        if (gameOver) {
            throw new GameOverException(winner != null ? winner.getName() : null);
        }
    }

    // -----------------------------------------------------------------------
    // Event-firing helpers
    // -----------------------------------------------------------------------

    private void fireGameStarted(int initialSum) {
        for (GameEventListener l : listeners) l.onGameStarted(initialSum);
    }

    private void fireTurnStarted(Player player) {
        for (GameEventListener l : listeners) l.onTurnStarted(player);
    }

    private void fireCardPlayed(Player player, Card card, int newSum) {
        for (GameEventListener l : listeners) l.onCardPlayed(player, card, newSum);
    }

    private void fireCardDrawn(Player player, Card card) {
        for (GameEventListener l : listeners) l.onCardDrawn(player, card);
    }

    private void firePlayerEliminated(Player player) {
        for (GameEventListener l : listeners) l.onPlayerEliminated(player);
    }

    private void fireGameOver(Player winningPlayer) {
        for (GameEventListener l : listeners) l.onGameOver(winningPlayer);
    }

    private void fireInvalidPlay(Player player, Card card, int currentSum) {
        for (GameEventListener l : listeners) l.onInvalidPlay(player, card, currentSum);
    }
}
