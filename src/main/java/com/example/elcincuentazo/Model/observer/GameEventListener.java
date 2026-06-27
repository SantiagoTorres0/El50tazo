package com.example.elcincuentazo.Model.observer;

import com.example.elcincuentazo.Model.Card;
import com.example.elcincuentazo.Model.Player;

/**
 * Observer interface that the View/Controller implements to react to game events.
 *
 * <p>The Model fires these callbacks on the JavaFX Application Thread when
 * significant game-state changes occur.  The View layer (FXML controller) is
 * expected to update the GUI accordingly.</p>
 *
 * <p>All methods have empty default implementations so that the View only
 * needs to override the events it cares about.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public interface GameEventListener {

    /**
     * Fired once after the game has been fully set up:
     * all players have received their 4 starting cards and the initial
     * table card has been placed.
     *
     * @param tableSum the initial sum on the table (0, 1, or -10 depending
     *                 on the first card drawn)
     */
    default void onGameStarted(int tableSum) {}

    /**
     * Fired at the beginning of a player's turn so the View can highlight
     * the active player and enable / disable controls.
     *
     * @param player the player whose turn is starting
     */
    default void onTurnStarted(Player player) {}

    /**
     * Fired after a player has successfully played a card onto the table.
     *
     * @param player   the player who played the card
     * @param card     the card that was played
     * @param newSum   the updated table sum after the card was played
     */
    default void onCardPlayed(Player player, Card card, int newSum) {}

    /**
     * Fired after a player has drawn a replacement card from the deck.
     *
     * @param player  the player who drew the card
     * @param card    the card drawn (visible only for the human player;
     *                for machines the View should display it face-down)
     */
    default void onCardDrawn(Player player, Card card) {}

    /**
     * Fired when the deck runs out of cards and is refilled by reshuffling
     * the table pile (excluding the top card).
     *
     * @param newDeckSize the number of cards now available in the refreshed deck
     */
    default void onDeckRefreshed(int newDeckSize) {}

    /**
     * Fired when a player is eliminated because none of their cards can be
     * played without exceeding a table sum of 50.
     *
     * @param player the eliminated player
     */
    default void onPlayerEliminated(Player player) {}

    /**
     * Fired when only one player remains and the game is over.
     *
     * @param winner the surviving (winning) player
     */
    default void onGameOver(Player winner) {}

    /**
     * Fired when an attempt to play an invalid card is detected (mainly
     * relevant for the human player's GUI interaction).
     *
     * @param player     the player who attempted the invalid play
     * @param card       the card that was rejected
     * @param currentSum the sum on the table at the time of the attempt
     */
    default void onInvalidPlay(Player player, Card card, int currentSum) {}
}
