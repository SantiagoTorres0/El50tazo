package com.example.elcincuentazo.Model;

import com.example.elcincuentazo.Model.exceptions.EmptyDeckException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Manages the draw pile (mazo) and the central table pile (mesa) for a
 * Cincuentazo game.
 *
 * <p>On construction a full 52-card deck is created (values 1–13 for each of
 * the 4 suits) and shuffled.  Cards are drawn from the top of the deck.
 * When the deck is exhausted, the table pile (excluding the top card, which
 * remains as the last played card) is reshuffled and becomes the new deck.</p>
 *
 * <p>The table pile is stored as a {@link Deque} to allow O(1) access to both
 * the top card and the full history.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class Deck {

    /** The draw pile; cards are dealt from the front (top). */
    private final Deque<Card> drawPile;

    /**
     * The table (mesa) pile.  Cards played by any player are pushed here.
     * Stored with the most-recently-played card at the front (peek = top).
     */
    private final Deque<Card> tablePile;

    /**
     * Constructs and fully initialises a new {@code Deck}.
     * Creates 52 cards (13 values × 4 suits), shuffles them, and places
     * them all in the draw pile.
     */
    public Deck() {
        drawPile  = new ArrayDeque<>(52);
        tablePile = new ArrayDeque<>(52);

        List<Card> cards = new ArrayList<>(52);
        for (int value = 1; value <= 13; value++) {
            for (int suit = 0; suit < 4; suit++) {
                cards.add(new Card(value, suit, 0));
            }
        }
        Collections.shuffle(cards);
        drawPile.addAll(cards);
    }

    // -----------------------------------------------------------------------
    // Draw operations
    // -----------------------------------------------------------------------

    /**
     * Draws and returns the top card from the draw pile.
     *
     * <p>If the draw pile is empty this method automatically refills it from
     * the table pile (all cards except the current top card) and reshuffles,
     * mirroring the official rule.  If refilling is also impossible (table
     * has only one card or no cards), an {@link EmptyDeckException} is thrown.
     * </p>
     *
     * @return the top {@link Card} from the draw pile (location set to 0)
     * @throws EmptyDeckException if the draw pile and table pile are both
     *                            exhausted
     */
    public Card drawCard() {
        if (drawPile.isEmpty()) {
            refillFromTablePile();
        }
        Card card = drawPile.poll();
        if (card == null) {
            throw new EmptyDeckException();
        }
        card.location = 0;
        return card;
    }

    /**
     * Places all cards from eliminated players back at the bottom of the draw
     * pile, as specified by the game rules.
     *
     * @param cards the list of cards to return to the bottom of the deck
     */
    public void returnCardsToBottom(List<Card> cards) {
        for (Card card : cards) {
            card.location = 0;
            drawPile.addLast(card);
        }
    }

    // -----------------------------------------------------------------------
    // Table-pile operations
    // -----------------------------------------------------------------------

    /**
     * Places a card onto the table pile (marks it as location 2).
     *
     * @param card the card being placed on the table; must not be {@code null}
     */
    public void placeOnTable(Card card) {
        card.location = 2;
        tablePile.addFirst(card);
    }

    /**
     * Returns the card currently on top of the table pile (the last one
     * played), without removing it.
     *
     * @return top card of the table pile, or {@code null} if the table is empty
     */
    public Card peekTableTop() {
        return tablePile.peekFirst();
    }

    /**
     * Returns an unmodifiable snapshot of the entire table pile in play order
     * (index 0 = most recently played).
     *
     * @return list of all table cards
     */
    public List<Card> getTablePile() {
        return Collections.unmodifiableList(new ArrayList<>(tablePile));
    }

    // -----------------------------------------------------------------------
    // Size / state queries
    // -----------------------------------------------------------------------

    /**
     * Returns the number of cards remaining in the draw pile.
     *
     * @return draw-pile size
     */
    public int drawPileSize() {
        return drawPile.size();
    }

    /**
     * Returns the number of cards currently on the table pile.
     *
     * @return table-pile size
     */
    public int tablePileSize() {
        return tablePile.size();
    }

    /**
     * Returns {@code true} if the draw pile is empty.
     *
     * @return {@code true} when no cards are left to draw
     */
    public boolean isDrawPileEmpty() {
        return drawPile.isEmpty();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Refills the draw pile from the table pile by moving all cards except
     * the topmost one into the draw pile and reshuffling them.
     *
     * <p>This implements the official rule: "If the deck runs out, take the
     * table cards except the last played one, shuffle them, and make them the
     * new deck. The table sum is not modified."</p>
     *
     * @throws EmptyDeckException if the table pile has fewer than 2 cards
     *                            (nothing to recycle)
     */
    private void refillFromTablePile() {
        if (tablePile.size() < 2) {
            throw new EmptyDeckException(
                "Cannot refill the deck: the table pile has " + tablePile.size() +
                " card(s) and the top card must remain."
            );
        }

        // Keep the top card on the table
        Card topCard = tablePile.pollFirst();

        // Move all remaining table cards to a list, shuffle, add to draw pile
        List<Card> recycled = new ArrayList<>(tablePile);
        tablePile.clear();
        Collections.shuffle(recycled);
        for (Card card : recycled) {
            card.location = 0;
        }
        drawPile.addAll(recycled);

        // Restore the top table card
        tablePile.addFirst(topCard);
    }
}
