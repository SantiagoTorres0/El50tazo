package com.example.elcincuentazo.exceptions;

import com.example.elcincuentazo.model.Deck;

/**
 * Unchecked (runtime) exception thrown when an attempt is made to draw a card
 * from a completely empty deck <em>and</em> the mesa (table pile) does not
 * have cards available to reshuffle.
 *
 * <p>Under normal game flow the {@link Deck}
 * refills itself automatically from the table pile, so this exception
 * should be rare.  It is unchecked because an empty-and-non-refillable deck
 * indicates a programming or state error rather than expected gameplay.</p>
 *
 * @author Mini-Proyecto 3 Team
 * @version 1.0
 */
public class EmptyDeckException extends RuntimeException {

    /** Serial version UID for serialization compatibility. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code EmptyDeckException} with a default message.
     */
    public EmptyDeckException() {
        super("The deck is empty and no cards are available from the table to reshuffle.");
    }

    /**
     * Constructs an {@code EmptyDeckException} with the given detail message.
     *
     * @param message explanation of why the deck is irrecoverably empty
     */
    public EmptyDeckException(String message) {
        super(message);
    }
}
