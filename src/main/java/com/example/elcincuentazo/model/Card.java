package com.example.elcincuentazo.model;

/**
 * Represents a single playing card in the Cincuentazo game.
 *
 * <p>A card has a numeric value (1–13), a suit (0–3), and a location
 * indicating where in the game it currently resides.</p>
 *
 * <p>Location codes:</p>
 * <ul>
 *   <li>{@code 0} – In the deck (mazo)</li>
 *   <li>{@code 1} – In a player's hand</li>
 *   <li>{@code 2} – On the central pile (mesa)</li>
 * </ul>
 *
 * <p>Suit codes:</p>
 * <ul>
 *   <li>{@code 0} – Spades (Picas)</li>
 *   <li>{@code 1} – Hearts (Corazones)</li>
 *   <li>{@code 2} – Clubs (Tréboles)</li>
 *   <li>{@code 3} – Diamonds (Diamantes)</li>
 * </ul>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class Card {

    /** Numeric face value of the card (1 = Ace, 11 = J, 12 = Q, 13 = K). */
    private int value;

    /** Suit of the card: 0=Spades, 1=Hearts, 2=Clubs, 3=Diamonds. */
    private int suit;

    /**
     * Current location of the card.
     * <ul>
     *   <li>0 – Deck</li>
     *   <li>1 – In hand</li>
     *   <li>2 – Central pile (mesa)</li>
     * </ul>
     */
    public int location;

    /**
     * Constructs a new {@code Card} with the given value, suit and location.
     *
     * @param value    face value of the card (1–13)
     * @param suit     suit code (0–3)
     * @param location initial location code (0–2)
     */
    public Card(int value, int suit, int location) {
        this.value = value;
        this.suit = suit;
        this.location = location;
    }

    /**
     * Returns the face value of this card.
     *
     * @return integer face value (1–13)
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the suit code of this card.
     *
     * @return suit code (0–3)
     */
    public int getSuit() {
        return suit;
    }

    /**
     * Returns a human-readable representation of this card,
     * e.g. {@code "As de Picas"} or {@code "7 de Corazones"}.
     *
     * @return display string for the card
     */
    @Override
    public String toString() {
        String suitName;
        switch (this.suit) {
            case 0:  suitName = "Picas";      break;
            case 1:  suitName = "Corazones";  break;
            case 2:  suitName = "Tréboles";   break;
            case 3:  suitName = "Diamantes";  break;
            default: suitName = "Desconocido";
        }

        String valueName;
        switch (this.value) {
            case 1:  valueName = "As"; break;
            case 11: valueName = "J";  break;
            case 12: valueName = "Q";  break;
            case 13: valueName = "K";  break;
            default: valueName = String.valueOf(this.value);
        }

        return valueName + " de " + suitName;
    }
}
