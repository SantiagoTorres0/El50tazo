package com.example.elcincuentazo.Model;

import com.example.elcincuentazo.Model.exceptions.EmptyDeckException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Deck}.
 *
 * <p>Validates:</p>
 * <ul>
 *   <li>Initial size and uniqueness of the 52-card deck.</li>
 *   <li>Drawing cards and tracking draw-pile size.</li>
 *   <li>Placing cards on the table pile and peeking at the top card.</li>
 *   <li>Auto-refill from the table pile when the draw pile is exhausted.</li>
 *   <li>{@link EmptyDeckException} thrown when neither pile can supply cards.</li>
 *   <li>Returning eliminated players' cards to the bottom of the deck.</li>
 * </ul>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
@DisplayName("Deck – data-structure unit tests")
class DeckTest {

    /** Fresh deck created before each test. */
    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    // ------------------------------------------------------------------
    // Initial state
    // ------------------------------------------------------------------

    @Test
    @DisplayName("A new deck contains exactly 52 cards")
    void newDeckHas52Cards() {
        assertEquals(52, deck.drawPileSize(),
            "A standard 52-card deck should be created");
    }

    @Test
    @DisplayName("A new deck contains 52 unique cards (no duplicates)")
    void newDeckHasNoDuplicates() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 52; i++) {
            Card c = deck.drawCard();
            String key = c.getValue() + "-" + c.getSuit();
            assertTrue(seen.add(key),
                "Duplicate card found: " + c);
        }
    }

    @Test
    @DisplayName("Table pile is initially empty")
    void tablePileInitiallyEmpty() {
        assertEquals(0, deck.tablePileSize());
        assertNull(deck.peekTableTop(), "No table card before any play");
    }

    // ------------------------------------------------------------------
    // Drawing cards
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Drawing a card reduces the deck size by 1")
    void drawingReducesDeckSize() {
        deck.drawCard();
        assertEquals(51, deck.drawPileSize());
    }

    @Test
    @DisplayName("Drawing all 52 cards leaves the deck empty")
    void drawAllCardsEmptiesDeck() {
        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }
        assertTrue(deck.isDrawPileEmpty());
    }

    // ------------------------------------------------------------------
    // Placing cards on the table
    // ------------------------------------------------------------------

    @Test
    @DisplayName("placeOnTable moves a card to the table pile")
    void placeOnTableAddsToTablePile() {
        Card card = deck.drawCard();
        deck.placeOnTable(card);
        assertEquals(1, deck.tablePileSize());
        assertEquals(card, deck.peekTableTop());
    }

    @Test
    @DisplayName("peekTableTop always returns the most recently placed card")
    void peekReturnsLastPlacedCard() {
        Card first  = deck.drawCard();
        Card second = deck.drawCard();
        deck.placeOnTable(first);
        deck.placeOnTable(second);
        assertEquals(second, deck.peekTableTop(),
            "Most recently placed card should be on top");
    }

    @Test
    @DisplayName("Card placed on table has location set to 2")
    void cardOnTableHasCorrectLocation() {
        Card card = deck.drawCard();
        deck.placeOnTable(card);
        assertEquals(2, card.location, "Table-pile cards should have location=2");
    }

    // ------------------------------------------------------------------
    // Auto-refill from table pile
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Deck auto-refills from table pile when draw pile is exhausted")
    void deckRefillsFromTablePile() {
        // Empty the draw pile
        for (int i = 0; i < 52; i++) {
            Card c = deck.drawCard();
            deck.placeOnTable(c);   // put them on the table
        }
        // Draw pile is empty, table has 52 cards.
        // Drawing one more should trigger refill (top card stays on table).
        assertTrue(deck.isDrawPileEmpty(), "Draw pile should be empty before refill");
        Card drawn = deck.drawCard(); // should trigger refill internally
        assertNotNull(drawn, "Should successfully draw after refill");
        // Table should keep only 1 card (the last-played top), rest are now in draw pile
        assertEquals(1, deck.tablePileSize(),
            "After refill, only the top card should remain on the table");
    }

    @Test
    @DisplayName("EmptyDeckException thrown when neither pile has cards")
    void emptyDeckExceptionWhenBothPilesEmpty() {
        // Draw all 52 and don't put any back on the table
        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }
        // Now both piles are empty — next draw should throw
        assertThrows(EmptyDeckException.class, deck::drawCard,
            "Should throw EmptyDeckException when draw pile and table pile are both empty");
    }

    // ------------------------------------------------------------------
    // Returning eliminated players' cards
    // ------------------------------------------------------------------

    @Test
    @DisplayName("returnCardsToBottom appends cards to the end of the draw pile")
    void returnCardsToBottomIncreasesSize() {
        // Draw 4 cards to simulate a player's hand
        java.util.List<Card> hand = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) hand.add(deck.drawCard());

        int sizeAfterDeal = deck.drawPileSize(); // 48
        deck.returnCardsToBottom(hand);
        assertEquals(sizeAfterDeal + 4, deck.drawPileSize(),
            "Returning 4 cards should increase deck size by 4");
    }

    @Test
    @DisplayName("Cards returned to deck have location reset to 0")
    void returnedCardsHaveLocationZero() {
        java.util.List<Card> hand = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Card c = deck.drawCard();
            c.location = 1; // simulate being in a hand
            hand.add(c);
        }
        deck.returnCardsToBottom(hand);
        for (Card c : hand) {
            assertEquals(0, c.location, "Returned cards should have location=0 (deck)");
        }
    }
}
