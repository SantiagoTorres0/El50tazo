package com.example.elcincuentazo.Model.exceptions;

/**
 * Checked exception thrown when a player attempts to play a card that would
 * cause the table sum to exceed 50.
 *
 * <p>This exception is <em>checked</em> so that callers are forced to handle
 * or declare the invalid-play scenario explicitly.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class InvalidCardPlayException extends Exception {

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /** The table sum at the moment of the illegal play attempt. */
    private final int currentSum;

    /** The delta the card would have added, causing the violation. */
    private final int attemptedDelta;

    /**
     * Constructs an {@code InvalidCardPlayException} with a descriptive message.
     *
     * @param message       human-readable explanation of the violation
     * @param currentSum    the table sum when the play was attempted
     * @param attemptedDelta the value the card would have contributed
     */
    public InvalidCardPlayException(String message, int currentSum, int attemptedDelta) {
        super(message);
        this.currentSum = currentSum;
        this.attemptedDelta = attemptedDelta;
    }

    /**
     * Returns the table sum that was active when the illegal play was attempted.
     *
     * @return current table sum
     */
    public int getCurrentSum() {
        return currentSum;
    }

    /**
     * Returns the delta value the attempted card would have contributed.
     *
     * @return attempted delta (may be negative for J/Q/K)
     */
    public int getAttemptedDelta() {
        return attemptedDelta;
    }

    /**
     * Returns a formatted string summarising the illegal play context.
     *
     * @return detail string
     */
    @Override
    public String toString() {
        return "InvalidCardPlayException{currentSum=" + currentSum
               + ", attemptedDelta=" + attemptedDelta
               + ", result=" + (currentSum + attemptedDelta)
               + "} → " + getMessage();
    }
}
