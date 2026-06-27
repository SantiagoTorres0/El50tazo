package com.example.elcincuentazo.Model.exceptions;

/**
 * Unchecked (runtime) exception thrown when an operation is requested on a
 * game that has already ended.
 *
 * <p>Examples of triggering scenarios:</p>
 * <ul>
 *   <li>A controller tries to advance a turn after the last player was
 *       declared winner.</li>
 *   <li>A thread attempts to schedule a machine move when the game is over.</li>
 * </ul>
 *
 * <p>This is unchecked because post-game operations indicate a logic error
 * in the caller rather than an expected gameplay situation.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class GameOverException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /** Name of the winning player, if known at the time the exception is thrown. */
    private final String winnerName;

    /**
     * Constructs a {@code GameOverException} without a known winner.
     */
    public GameOverException() {
        super("The game has already ended.");
        this.winnerName = null;
    }

    /**
     * Constructs a {@code GameOverException} identifying the winner.
     *
     * @param winnerName display name of the player who won the game
     */
    public GameOverException(String winnerName) {
        super("The game has ended. Winner: " + winnerName);
        this.winnerName = winnerName;
    }

    /**
     * Returns the name of the winner, or {@code null} if not provided.
     *
     * @return winner's name or {@code null}
     */
    public String getWinnerName() {
        return winnerName;
    }
}
