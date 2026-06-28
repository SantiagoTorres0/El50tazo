package com.example.elcincuentazo.model;

import com.example.elcincuentazo.exceptions.InvalidCardPlayException;

import java.util.List;

/**
 * Abstract base class representing a participant in a Cincuentazo game.
 *
 * <p>A player holds a {@link PlayerHand} of up to 4 cards and can be either
 * active (still in the game) or eliminated.  Concrete subclasses define
 * whether the player is human ({@link HumanPlayer}) or machine
 * ({@link MachinePlayer}).</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see HumanPlayer
 * @see MachinePlayer
 */
public abstract class Player {

    /** Display name shown in the GUI, e.g. "Jugador" or "Máquina 1". */
    private final String name;

    /** The cards currently in this player's possession. */
    private final PlayerHand hand;

    /** Whether this player is still participating in the game. */
    private boolean active;

    /**
     * Constructs a new player with the given name.
     * The player starts with an empty hand and is active by default.
     *
     * @param name display name for this player; must not be {@code null}
     */
    public Player(String name) {
        this.name = name;
        this.hand = new PlayerHand();
        this.active = true;
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the display name of this player.
     *
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the hand of cards held by this player.
     *
     * @return this player's {@link PlayerHand}
     */
    public PlayerHand getHand() {
        return hand;
    }

    /**
     * Returns {@code true} if the player is still in the game.
     *
     * @return {@code true} when active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Marks this player as eliminated (sets active to {@code false}).
     * Should only be called by {@link GameLogic} after the elimination
     * condition has been confirmed.
     */
    public void eliminate() {
        this.active = false;
    }

    // -----------------------------------------------------------------------
    // Card-play helpers
    // -----------------------------------------------------------------------

    /**
     * Checks whether at least one card in this player's hand can legally be
     * played without causing the table sum to exceed 50.
     *
     * @param currentSum the current sum on the table
     * @return {@code true} if there is at least one playable card
     */
    public boolean hasPlayableCard(int currentSum) {
        for (Card card : hand.getCards()) {
            if (CardValueCalculator.isPlayable(card, currentSum)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable view of the cards currently in this player's
     * hand.  Convenience wrapper around {@link PlayerHand#getCards()}.
     *
     * @return list of cards
     */
    public List<Card> getCards() {
        return hand.getCards();
    }

    /**
     * Adds a card to this player's hand (e.g. after drawing from the deck).
     *
     * @param card the card to add; must not be {@code null}
     */
    public void receiveCard(Card card) {
        hand.addCard(card);
    }

    /**
     * Plays the card at the given hand index.
     * Validates the play against {@code currentSum} before removing the card.
     *
     * @param index      zero-based index of the card to play
     * @param currentSum the current table sum
     * @return the played {@link Card}
     * @throws InvalidCardPlayException if the card would cause the sum to exceed 50
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public Card playCard(int index, int currentSum) throws InvalidCardPlayException {
        Card card = hand.getCard(index);
        int delta = CardValueCalculator.calculateDelta(card, currentSum);
        if (currentSum + delta > 50) {
            throw new InvalidCardPlayException(
                "Playing " + card + " would raise the sum to " + (currentSum + delta),
                currentSum,
                delta
            );
        }
        return hand.removeCard(index);
    }

    /**
     * Plays a specific card instance from this player's hand.
     *
     * @param card       the card to play
     * @param currentSum the current table sum
     * @return the played {@link Card}
     * @throws InvalidCardPlayException if the play is illegal
     * @throws IllegalArgumentException if the card is not in this player's hand
     */
    public Card playCard(Card card, int currentSum) throws InvalidCardPlayException {
        List<Card> cards = hand.getCards();
        int index = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) == card) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException("Card " + card + " is not in " + name + "'s hand.");
        }
        return playCard(index, currentSum);
    }

    /**
     * Returns a display-friendly description of this player's state.
     *
     * @return formatted string with name, status and hand contents
     */
    @Override
    public String toString() {
        return name + " [" + (active ? "active" : "eliminated") + "] Hand: " + hand;
    }

    /**
     * Indicates whether this player is human or machine-controlled.
     * Subclasses must implement this to allow the engine to differentiate
     * between GUI interaction and AI decision-making.
     *
     * @return {@code true} for a human player, {@code false} for a machine
     */
    public abstract boolean isHuman();
}
