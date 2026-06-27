package com.example.elcincuentazo.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the set of cards currently held by one player.
 *
 * <p>A {@code PlayerHand} always starts empty and cards are added
 * individually via {@link #addCard(Card)}.  Cards may be removed when
 * the player plays one or when the player is eliminated.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class PlayerHand {

    /** Maximum number of cards a player is allowed to hold at one time. */
    public static final int MAX_CARDS = 4;

    /** Internal list holding the cards in this hand. */
    private final List<Card> hand;

    /**
     * Constructs an empty {@code PlayerHand}.
     */
    public PlayerHand() {
        this.hand = new ArrayList<>();
    }

    /**
     * Adds a card to this hand and marks it as being in a player's hand
     * (location = 1).
     *
     * @param card the {@link Card} to add; must not be {@code null}
     */
    public void addCard(Card card) {
        card.location = 1;
        this.hand.add(card);
    }

    /**
     * Removes and returns the card at the specified index.
     *
     * @param index zero-based index of the card to remove
     * @return the removed {@link Card}
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public Card removeCard(int index) {
        return hand.remove(index);
    }

    /**
     * Removes a specific card instance from the hand.
     *
     * @param card the card to remove
     * @return {@code true} if the card was present and removed
     */
    public boolean removeCard(Card card) {
        return hand.remove(card);
    }

    /**
     * Returns the card at the given index without removing it.
     *
     * @param index zero-based index
     * @return the {@link Card} at that position
     */
    public Card getCard(int index) {
        return hand.get(index);
    }

    /**
     * Returns an unmodifiable view of all cards currently in this hand.
     *
     * @return unmodifiable {@link List} of cards
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Returns the number of cards currently in this hand.
     *
     * @return card count
     */
    public int size() {
        return hand.size();
    }

    /**
     * Returns {@code true} if the hand contains no cards.
     *
     * @return {@code true} when empty
     */
    public boolean isEmpty() {
        return hand.isEmpty();
    }

    /**
     * Returns a display string listing all cards in this hand.
     *
     * @return comma-separated card descriptions, or {@code "(empty)"} if none
     */
    @Override
    public String toString() {
        if (hand.isEmpty()) return "(empty)";
        return hand.stream()
                   .map(Card::toString)
                   .collect(Collectors.joining(", "));
    }
}
