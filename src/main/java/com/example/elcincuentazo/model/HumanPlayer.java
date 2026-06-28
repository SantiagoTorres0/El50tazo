package com.example.elcincuentazo.model;

/**
 * Represents the human player in a Cincuentazo game.
 *
 * <p>The human player's cards are displayed face-up in the GUI, and moves
 * are initiated by mouse-click events handled in the Controller layer.
 * This class contains no AI logic; all decisions come from the user.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 * @see Player
 * @see MachinePlayer
 */
public class HumanPlayer extends Player {

    /**
     * Constructs a human player with the display name {@code "Jugador"}.
     */
    public HumanPlayer() {
        super("Jugador");
    }

    /**
     * Constructs a human player with a custom display name.
     *
     * @param name display name for this human player
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Always returns {@code true} because this is a human-controlled player.
     *
     * @return {@code true}
     */
    @Override
    public boolean isHuman() {
        return true;
    }
}
