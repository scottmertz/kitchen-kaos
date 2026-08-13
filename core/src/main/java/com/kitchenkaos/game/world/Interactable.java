package com.kitchenkaos.game.world;

import com.badlogic.gdx.math.Rectangle;

/**
 * Anything the player can walk up to, face, and press SPACE on.
 * Stations implement this now; POS and the ticket printer will
 * implement it too once they exist (Step 4+).
 */
public interface Interactable {
    Rectangle getBounds();

    /** Short text for an on-screen prompt, e.g. "GRILL Station". */
    String getLabel();

    void interact();
}