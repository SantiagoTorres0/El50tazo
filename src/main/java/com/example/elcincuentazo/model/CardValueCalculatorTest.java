package com.example.elcincuentazo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CardValueCalculator}.
 *
 * <p>Covers every card-value rule specified in the Cincuentazo game rules:</p>
 * <ul>
 *   <li>2–8, 10 → add face value</li>
 *   <li>9 → neutral (delta = 0)</li>
 *   <li>J, Q, K → subtract 10</li>
 *   <li>Ace → add 10 if sum stays ≤ 50, else add 1</li>
 *   <li>{@code isPlayable} boundary conditions</li>
 * </ul>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
@DisplayName("CardValueCalculator – game-rule unit tests")
class CardValueCalculatorTest {

    // ------------------------------------------------------------------
    // Helper: create a card with any suit (suit is irrelevant for value)
    // ------------------------------------------------------------------

    private Card card(int value) {
        return new Card(value, 0, 1);
    }

    // ------------------------------------------------------------------
    // 2-8 and 10 → add face value
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "Card {0} should add {0} to the sum")
    @CsvSource({"2,2", "3,3", "4,4", "5,5", "6,6", "7,7", "8,8", "10,10"})
    @DisplayName("Numbered cards 2–8 and 10 add their face value")
    void numberedCardsAddFaceValue(int face, int expectedDelta) {
        int delta = CardValueCalculator.calculateDelta(card(face), 20);
        assertEquals(expectedDelta, delta,
            "Card " + face + " should contribute " + expectedDelta);
    }

    // ------------------------------------------------------------------
    // 9 → neutral
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Card 9 contributes 0 (neutral)")
    void nineIsNeutral() {
        assertEquals(0, CardValueCalculator.calculateDelta(card(9), 30),
            "Card 9 should not change the sum");
    }

    // ------------------------------------------------------------------
    // J (11), Q (12), K (13) → subtract 10
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "Face card {0} should subtract 10")
    @CsvSource({"11", "12", "13"})
    @DisplayName("J, Q, K each subtract 10 from the table sum")
    void faceCardsSubtractTen(int face) {
        int delta = CardValueCalculator.calculateDelta(card(face), 40);
        assertEquals(-10, delta, "Face card " + face + " should give delta -10");
    }

    // ------------------------------------------------------------------
    // Ace logic
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Ace selects +10 when 10 keeps sum ≤ 50")
    void aceChoosesTenWhenSafe() {
        // currentSum=35 → 35+10=45 ≤ 50  →  should pick 10
        assertEquals(10, CardValueCalculator.calculateDelta(card(1), 35));
    }

    @Test
    @DisplayName("Ace selects +1 when +10 would exceed 50")
    void aceChoosesOneWhenTenExceeds() {
        // currentSum=45 → 45+10=55 > 50, 45+1=46 ≤ 50  →  should pick 1
        assertEquals(1, CardValueCalculator.calculateDelta(card(1), 45));
    }

    @Test
    @DisplayName("Ace on sum=40 should pick +10 (exactly 50)")
    void aceOnFortyPicksTen() {
        // 40+10=50 is exactly 50 → valid, prefer 10
        assertEquals(10, CardValueCalculator.calculateDelta(card(1), 40));
    }

    // ------------------------------------------------------------------
    // isPlayable boundary tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("isPlayable returns true when resulting sum equals exactly 50")
    void isPlayableReturnsTrueAtExactly50() {
        // card 5 on sum 45 → 50 (boundary, allowed)
        assertTrue(CardValueCalculator.isPlayable(card(5), 45));
    }

    @Test
    @DisplayName("isPlayable returns false when resulting sum would be 51")
    void isPlayableReturnsFalseAt51() {
        // card 6 on sum 45 → 51 (over limit)
        assertFalse(CardValueCalculator.isPlayable(card(6), 45));
    }

    @Test
    @DisplayName("K (subtract 10) is always playable even at sum=50")
    void kingIsAlwaysPlayableAtMax() {
        assertTrue(CardValueCalculator.isPlayable(card(13), 50),
            "K on sum=50 gives 40, which is ≤ 50");
    }

    @Test
    @DisplayName("Card 9 is always playable regardless of current sum")
    void nineIsAlwaysPlayable() {
        assertTrue(CardValueCalculator.isPlayable(card(9), 50),
            "9 on sum=50 gives 50, which is ≤ 50");
    }

    // ------------------------------------------------------------------
    // describeEffect sanity check
    // ------------------------------------------------------------------

    @Test
    @DisplayName("describeEffect produces a non-empty string")
    void describeEffectIsNotEmpty() {
        String description = CardValueCalculator.describeEffect(card(7), 30);
        assertNotNull(description);
        assertFalse(description.isBlank(), "Effect description should not be blank");
    }

    @Test
    @DisplayName("describeEffect for 9 mentions neutral wording")
    void describeEffectForNineIsNeutral() {
        String description = CardValueCalculator.describeEffect(card(9), 20);
        assertTrue(description.contains("no suma ni resta"),
            "Expected neutral description for card 9, got: " + description);
    }
}
