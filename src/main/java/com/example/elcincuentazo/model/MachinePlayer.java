package com.example.elcincuentazo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AI-controlled player in a Cincuentazo game.
 *
 * <p>Machine players use a simple greedy strategy: they choose the playable
 * card whose delta brings the table sum as close to 50 as possible without
 * exceeding it.  This maximises pressure on the remaining human and machine
 * opponents.</p>
 *
 * <p>Timing (2–4 s to "think", 1–2 s to draw) is enforced by
 * {@link TurnManager} rather than inside this class, keeping the model
 * free of threading concerns.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see Player
 * @see HumanPlayer
 * @see TurnManager
 */
public class MachinePlayer extends Player {

    /**
     * Constructs a machine player with a numbered display name,
     * e.g. {@code "Máquina 1"}.
     *
     * @param machineNumber 1-based index used to distinguish multiple machines
     */
    public MachinePlayer(int machineNumber) {
        super("Máquina " + machineNumber);
    }

    /**
     * Always returns {@code false} because this is an AI-controlled player.
     *
     * @return {@code false}
     */
    @Override
    public boolean isHuman() {
        return false;
    }

    /**
     * Selects the best card to play from the machine's hand using a greedy
     * strategy.
     *
     * <p>Strategy:</p>
     * <ol>
     *   <li>Collect all playable cards (those that keep the sum ≤ 50).</li>
     *   <li>Among those, prefer the card that results in the highest table sum
     *       (maximises pressure on opponents).</li>
     *   <li>If multiple cards produce the same resulting sum, pick the first
     *       one found.</li>
     * </ol>
     *
     * @param currentSum the current table sum before this machine plays
     * @return the chosen {@link Card}, or {@code null} if no card is playable
     *         (the machine should then be eliminated by {@link GameLogic})
     */
    public Card chooseBestCard(int currentSum) {
        List<Card> cards = getHand().getCards();
        Card bestCard = null;
        int bestResultingSum = Integer.MIN_VALUE;

        for (Card card : cards) {
            int delta = CardValueCalculator.calculateDelta(card, currentSum);
            int resultingSum = currentSum + delta;
            if (resultingSum <= 50 && resultingSum > bestResultingSum) {
                bestResultingSum = resultingSum;
                bestCard = card;
            }
        }

        return bestCard;
    }

    /**
     * Returns all cards in the machine's hand that could legally be played.
     *
     * @param currentSum the current table sum
     * @return list of playable {@link Card}s; empty if none can be played
     */
    public List<Card> getPlayableCards(int currentSum) {
        List<Card> playable = new ArrayList<>();
        for (Card card : getHand().getCards()) {
            if (CardValueCalculator.isPlayable(card, currentSum)) {
                playable.add(card);
            }
        }
        return playable;
    }
}
