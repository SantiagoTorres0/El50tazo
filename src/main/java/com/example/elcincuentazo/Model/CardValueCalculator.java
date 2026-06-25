package com.example.elcincuentazo.Model;

/**
 * Utility class that encapsulates the card-value rules for Cincuentazo.
 *
 * <p>Rules applied to a card's face value:</p>
 * <ul>
 *   <li>2–8, 10 → add the face value to the table sum.</li>
 *   <li>9 → neither adds nor subtracts (contributes 0).</li>
 *   <li>J (11), Q (12), K (13) → subtract 10 from the table sum.</li>
 *   <li>A (1) → add either 1 or 10, whichever does <em>not</em> cause the
 *       table sum to exceed 50; if both exceed 50, treat as +1 (the lesser
 *       evil kept for elimination checking).</li>
 * </ul>
 *
 * <p>This class is stateless and cannot be instantiated.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public final class CardValueCalculator {

    /** Private constructor – utility class, do not instantiate. */
    private CardValueCalculator() {}

    /**
     * Calculates the point contribution of a card given the current table sum.
     *
     * <p>For an Ace, this method picks the value (1 or 10) that keeps the
     * table sum at or below 50.  If both options exceed 50, it returns 1
     * so the caller can detect the elimination condition.</p>
     *
     * @param card         the card being evaluated; must not be {@code null}
     * @param currentSum   the current accumulated sum on the table (mesa)
     * @return the delta to apply to {@code currentSum} (can be negative for
     *         J/Q/K, zero for 9, positive otherwise)
     */
    public static int calculateDelta(Card card, int currentSum) {
        int face = card.getValue();

        if (face == 9) {
            return 0;
        }

        if (face == 11 || face == 12 || face == 13) {
            return -10;
        }

        if (face == 1) {
            // Ace: choose 1 or 10 — whichever keeps sum ≤ 50
            if (currentSum + 10 <= 50) {
                return 10;
            }
            return 1;
        }

        // face 2–8, 10
        return face;
    }

    /**
     * Calculates the best possible delta for an Ace explicitly, returning
     * either {@code 1} or {@code 10}.
     *
     * @param currentSum the current table sum
     * @return {@code 10} if it does not exceed 50; {@code 1} otherwise
     */
    public static int aceOptimalDelta(int currentSum) {
        return (currentSum + 10 <= 50) ? 10 : 1;
    }

    /**
     * Determines whether playing a given card would keep the table sum at or
     * below 50 (i.e. the play is valid).
     *
     * @param card       the card to evaluate
     * @param currentSum the current table sum
     * @return {@code true} if the card may legally be played
     */
    public static boolean isPlayable(Card card, int currentSum) {
        int delta = calculateDelta(card, currentSum);
        return (currentSum + delta) <= 50;
    }

    /**
     * Returns a human-readable description of how a card affects the table
     * sum, useful for logging and GUI tooltips.
     *
     * @param card       the card in question
     * @param currentSum the current table sum before the card is played
     * @return description string, e.g. {@code "K de Picas resta 10"}
     */
    public static String describeEffect(Card card, int currentSum) {
        int delta = calculateDelta(card, currentSum);
        if (delta == 0) return card + " no suma ni resta";
        if (delta > 0)  return card + " suma " + delta;
        return card + " resta " + Math.abs(delta);
    }
}
