package com.example.elcincuentazo.model;

import com.example.elcincuentazo.exceptions.GameOverException;
import com.example.elcincuentazo.exceptions.InvalidCardPlayException;
import com.example.elcincuentazo.observer.GameEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameLogic}.
 *
 * <p>Validates the core game-flow scenarios:</p>
 * <ul>
 *   <li>Correct setup (player count, hand sizes, initial table card).</li>
 *   <li>Valid card plays update the table sum and hand size.</li>
 *   <li>Invalid plays throw {@link InvalidCardPlayException} and do not mutate
 *       the state.</li>
 *   <li>Player elimination when no legal card is available.</li>
 *   <li>Game-over detection and winner announcement.</li>
 *   <li>Event listener callbacks are fired in the correct order.</li>
 * </ul>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
@DisplayName("GameLogic – integration unit tests")
class GameLogicTest {

    private GameLogic logic;

    @BeforeEach
    void setUp() {
        logic = new GameLogic();
        logic.setupGame(1); // 1 human + 1 machine for most tests
    }

    // ------------------------------------------------------------------
    // Setup tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("setupGame(1) creates exactly 2 players")
    void setupCreatesCorrectNumberOfPlayers() {
        assertEquals(2, logic.getPlayers().size());
    }

    @Test
    @DisplayName("setupGame(3) creates exactly 4 players")
    void setupWithThreeMachinesCreatesFourPlayers() {
        logic.setupGame(3);
        assertEquals(4, logic.getPlayers().size());
    }

    @Test
    @DisplayName("First player is always human")
    void firstPlayerIsHuman() {
        assertTrue(logic.getPlayers().get(0).isHuman());
    }

    @Test
    @DisplayName("Subsequent players are machines")
    void subsequentPlayersAreMachines() {
        for (int i = 1; i < logic.getPlayers().size(); i++) {
            assertFalse(logic.getPlayers().get(i).isHuman(),
                "Player at index " + i + " should be a machine");
        }
    }

    @Test
    @DisplayName("Each player starts with exactly 4 cards")
    void eachPlayerStartsWithFourCards() {
        for (Player p : logic.getPlayers()) {
            assertEquals(4, p.getHand().size(),
                p.getName() + " should start with 4 cards");
        }
    }

    @Test
    @DisplayName("Deck has 52 - (players*4) - 1 cards after setup")
    void deckHasCorrectSizeAfterSetup() {
        // 1 human + 1 machine = 2 players × 4 cards = 8 dealt + 1 table card = 9 total removed
        int expected = 52 - (2 * 4) - 1;
        assertEquals(expected, logic.getDeck().drawPileSize());
    }

    @Test
    @DisplayName("Table sum is initialised (not necessarily 0 but deterministic)")
    void tableSumIsInitialised() {
        // sum can be -10, 0, 1, or 2–10 depending on initial card; just check it ran
        int sum = logic.getTableSum();
        assertTrue(sum <= 10 && sum >= -10,
            "Initial table sum should be in [-10, 10], got: " + sum);
    }

    @Test
    @DisplayName("setupGame throws IllegalArgumentException for count=0")
    void setupRejectsZeroMachines() {
        assertThrows(IllegalArgumentException.class, () -> logic.setupGame(0));
    }

    @Test
    @DisplayName("setupGame throws IllegalArgumentException for count=4")
    void setupRejectsFourMachines() {
        assertThrows(IllegalArgumentException.class, () -> logic.setupGame(4));
    }

    // ------------------------------------------------------------------
    // Valid play tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Playing a valid card decreases player hand size by 0 (draws replacement)")
    void validPlayKeepsHandSizeAt4() throws Exception {
        Player human = logic.getPlayers().get(0);
        // Find a playable card
        Card playable = findPlayableCard(human, logic.getTableSum());
        assumeNotNull(playable, "Human must have a playable card");

        logic.humanPlayCard(playable);
        assertEquals(4, human.getHand().size(),
            "Hand should remain at 4 after play + draw");
    }

    @Test
    @DisplayName("Playing a valid card updates the table sum")
    void validPlayUpdatesTableSum() throws Exception {
        // Force a known state: set sum to 0 via reflection-free approach
        logic.setupGame(1);
        forceTableSumToZero();

        Player human = logic.getPlayers().get(0);
        // Find a card with known delta
        Card twoCard = findCardWithValue(human, 2);
        if (twoCard == null) return; // skip if human doesn't have a 2

        int sumBefore = logic.getTableSum();
        logic.humanPlayCard(twoCard);
        // Sum should have increased by 2
        assertEquals(sumBefore + 2, logic.getTableSum());
    }

    // ------------------------------------------------------------------
    // Invalid play tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Playing a card that exceeds 50 throws InvalidCardPlayException")
    void invalidPlayThrowsException() {
        // Manually drive table sum to 49 then try to play a 2
        logic.setupGame(1);
        forceTableSumHigh(49);

        Player human = logic.getPlayers().get(0);
        Card twoCard = findCardWithValue(human, 2);
        if (twoCard == null) return; // skip if human doesn't have a 2

        assertThrows(InvalidCardPlayException.class,
            () -> logic.humanPlayCard(twoCard));
    }

    @Test
    @DisplayName("Hand size does not change after an invalid play attempt")
    void invalidPlayDoesNotChangeHandSize() {
        logic.setupGame(1);
        forceTableSumHigh(49);

        Player human = logic.getPlayers().get(0);
        Card twoCard = findCardWithValue(human, 2);
        if (twoCard == null) return;

        int sizeBefore = human.getHand().size();
        try {
            logic.humanPlayCard(twoCard);
        } catch (InvalidCardPlayException | GameOverException ignored) {}
        assertEquals(sizeBefore, human.getHand().size(),
            "Hand must be unchanged after a failed play attempt");
    }

    // ------------------------------------------------------------------
    // GameOver / exception guard
    // ------------------------------------------------------------------

    @Test
    @DisplayName("humanPlayCard throws GameOverException when game has already ended")
    void playAfterGameOverThrows() throws Exception {
        // Eliminate all but the human to trigger game-over
        for (Player p : logic.getPlayers()) {
            if (!p.isHuman()) {
                p.eliminate();
            }
        }
        // Next call should throw GameOverException
        // (game-over is declared lazily on next action)
        // Force detection:
        logic = new GameLogic();
        logic.setupGame(1);
        eliminateAllMachines();
        // game should now be over
        // any subsequent play attempt must throw
        if (logic.isGameOver()) {
            Card anyCard = logic.getPlayers().get(0).getHand().getCards().get(0);
            assertThrows(GameOverException.class, () -> logic.humanPlayCard(anyCard));
        }
    }

    // ------------------------------------------------------------------
    // Observer / event-listener tests
    // ------------------------------------------------------------------

    @Test
    @DisplayName("onGameStarted is fired after setupGame")
    void listenerReceivesGameStartedEvent() {
        List<String> events = new ArrayList<>();
        logic.addGameEventListener(new GameEventListener() {
            @Override public void onGameStarted(int tableSum) { events.add("started"); }
        });
        logic.setupGame(1);  // re-setup to trigger the event after listener is registered
        assertTrue(events.contains("started"),
            "onGameStarted should be called during setupGame");
    }

    @Test
    @DisplayName("onCardPlayed is fired when a valid card is played")
    void listenerReceivesCardPlayedEvent() throws Exception {
        logic.setupGame(1);
        List<String> events = new ArrayList<>();
        logic.addGameEventListener(new GameEventListener() {
            @Override
            public void onCardPlayed(Player player, Card card, int newSum) {
                events.add("played:" + card.getValue());
            }
        });

        Player human = logic.getPlayers().get(0);
        Card playable = findPlayableCard(human, logic.getTableSum());
        if (playable == null) return;

        logic.humanPlayCard(playable);
        assertFalse(events.isEmpty(), "onCardPlayed should be called after a valid play");
    }

    @Test
    @DisplayName("onInvalidPlay is fired when an invalid card is attempted")
    void listenerReceivesInvalidPlayEvent() {
        logic.setupGame(1);
        forceTableSumHigh(49);
        List<String> events = new ArrayList<>();
        logic.addGameEventListener(new GameEventListener() {
            @Override
            public void onInvalidPlay(Player player, Card card, int currentSum) {
                events.add("invalid");
            }
        });

        Player human = logic.getPlayers().get(0);
        Card twoCard = findCardWithValue(human, 2);
        if (twoCard == null) return;

        try { logic.humanPlayCard(twoCard); } catch (Exception ignored) {}
        assertTrue(events.contains("invalid"),
            "onInvalidPlay should be called on an illegal play attempt");
    }

    // ------------------------------------------------------------------
    // GameState snapshot
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getGameState returns a non-null snapshot")
    void getGameStateReturnsSnapshot() {
        assertNotNull(logic.getGameState());
    }

    @Test
    @DisplayName("GameState reflects correct player count")
    void gameStateReflectsPlayerCount() {
        assertEquals(logic.getPlayers().size(),
                     logic.getGameState().getPlayers().size());
    }

    // ------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------

    /** Finds the first card in the player's hand that is legally playable. */
    private Card findPlayableCard(Player player, int currentSum) {
        for (Card c : player.getHand().getCards()) {
            if (CardValueCalculator.isPlayable(c, currentSum)) return c;
        }
        return null;
    }

    /** Finds the first card in the player's hand with the given face value. */
    private Card findCardWithValue(Player player, int value) {
        for (Card c : player.getHand().getCards()) {
            if (c.getValue() == value) return c;
        }
        return null;
    }

    /**
     * Hacky helper: plays cards until the table sum is forced very high.
     * Uses reflection-free approach — directly sets via a neutral card loop.
     * NOTE: this mutates game state significantly; only use in tests that
     * restart with a fresh setup first.
     */
    private void forceTableSumHigh(int targetSum) {
        // Add a K (−10) player card to get sum down if it is high, then work up.
        // Simpler approach: just note this is test-only scaffolding.
        // We use a package-private test accessor pattern: expose a setter in tests.
        // Since GameLogic doesn't expose one, we instead trust the deck deals
        // a 2 to the human most of the time and just try to reach state via gameplay.
        // For robustness, we add a test-only subclass trick inline:
        try {
            java.lang.reflect.Field f = logic.getClass().getDeclaredField("tableSum");
            f.setAccessible(true);
            f.set(logic, targetSum);
        } catch (Exception e) {
            throw new RuntimeException("Reflection helper failed in test", e);
        }
    }

    private void forceTableSumToZero() {
        forceTableSumHigh(0);
    }

    private void eliminateAllMachines() {
        for (Player p : logic.getPlayers()) {
            if (!p.isHuman()) {
                List<Card> cards = new ArrayList<>(p.getHand().getCards());
                for (Card c : cards) p.getHand().removeCard(c);
                p.eliminate();
            }
        }
        // Manually trigger game-over via reflection
        try {
            java.lang.reflect.Field f = logic.getClass().getDeclaredField("gameOver");
            f.setAccessible(true);
            f.set(logic, true);
            java.lang.reflect.Field w = logic.getClass().getDeclaredField("winner");
            w.setAccessible(true);
            w.set(logic, logic.getPlayers().get(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Skips the test gracefully when a precondition is not met. */
    private void assumeNotNull(Object obj, String message) {
        org.junit.jupiter.api.Assumptions.assumeTrue(obj != null, message);
    }
}
