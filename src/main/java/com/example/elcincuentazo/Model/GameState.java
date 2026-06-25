package com.example.elcincuentazo.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the game state at a specific point in time.
 *
 * <p>The Controller can request a {@code GameState} from {@link GameLogic} at
 * any moment and pass it to the View for rendering, without the View needing
 * direct access to the mutable model objects.</p>
 *
 * <p>All collections returned by this class are <em>unmodifiable</em>.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class GameState {

    /** Ordered list of all players (active and eliminated). */
    private final List<Player> players;

    /** The card currently on top of the table pile. */
    private final Card topTableCard;

    /** Current accumulated sum on the table (mesa). */
    private final int tableSum;

    /** Number of cards remaining in the draw pile. */
    private final int deckSize;

    /** The player whose turn it currently is, or {@code null} if game is over. */
    private final Player currentPlayer;

    /** The winner of the game, or {@code null} if the game is still in progress. */
    private final Player winner;

    /** Whether the game has finished. */
    private final boolean gameOver;

    /**
     * Constructs a new {@code GameState} snapshot.
     *
     * @param players       list of all players (copied defensively)
     * @param topTableCard  top card on the table pile (may be {@code null} before first play)
     * @param tableSum      current table sum
     * @param deckSize      number of cards in the draw pile
     * @param currentPlayer player whose turn it is ({@code null} if game over)
     * @param winner        winning player ({@code null} if game still in progress)
     * @param gameOver      {@code true} when the game has ended
     */
    public GameState(
            List<Player> players,
            Card topTableCard,
            int tableSum,
            int deckSize,
            Player currentPlayer,
            Player winner,
            boolean gameOver
    ) {
        this.players       = Collections.unmodifiableList(new ArrayList<>(players));
        this.topTableCard  = topTableCard;
        this.tableSum      = tableSum;
        this.deckSize      = deckSize;
        this.currentPlayer = currentPlayer;
        this.winner        = winner;
        this.gameOver      = gameOver;
    }

    /**
     * Returns an unmodifiable list of all players (active and eliminated),
     * in turn order.
     *
     * @return player list
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns only the active (non-eliminated) players.
     *
     * @return list of active players
     */
    public List<Player> getActivePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player p : players) {
            if (p.isActive()) active.add(p);
        }
        return Collections.unmodifiableList(active);
    }

    /**
     * Returns the card currently on top of the table pile.
     *
     * @return top table card, or {@code null} before the first card is placed
     */
    public Card getTopTableCard() {
        return topTableCard;
    }

    /**
     * Returns the current accumulated table sum.
     *
     * @return table sum (may be negative if J/Q/K started the game)
     */
    public int getTableSum() {
        return tableSum;
    }

    /**
     * Returns the number of cards remaining in the draw pile.
     *
     * @return deck size
     */
    public int getDeckSize() {
        return deckSize;
    }

    /**
     * Returns the player who is currently taking their turn.
     *
     * @return current player, or {@code null} if the game is over
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the winning player if the game has ended.
     *
     * @return winner, or {@code null} if the game is still in progress
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Returns {@code true} if the game has ended.
     *
     * @return {@code true} when game over
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns a multi-line debug summary of this snapshot.
     *
     * @return readable state description
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== GameState ===\n");
        sb.append("Table sum : ").append(tableSum).append("\n");
        sb.append("Top card  : ").append(topTableCard).append("\n");
        sb.append("Deck size : ").append(deckSize).append("\n");
        sb.append("Current   : ").append(currentPlayer != null ? currentPlayer.getName() : "none").append("\n");
        sb.append("Game over : ").append(gameOver)
          .append(winner != null ? " → Winner: " + winner.getName() : "").append("\n");
        for (Player p : players) {
            sb.append("  ").append(p).append("\n");
        }
        return sb.toString();
    }
}
