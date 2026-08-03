package com.kitchenkaos.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * Root of the game. This is the platform-independent entry point —
 * Lwjgl3Launcher (desktop) hands control to this class and never
 * touches gameplay logic directly. Right now this just proves the
 * render loop is alive by clearing the screen; ShiftScreen and the
 * rest of Part 1's systems get wired in later.
 */
public class KitchenKaosGame extends Game {

    @Override
    public void create() {
        // Nothing to set up yet — screens get added in a later step.
    }

    @Override
    public void render() {
        // Clear the screen each frame. Colors are 0f-1f, not 0-255.
        // This dark charcoal is just a placeholder so we can visually
        // confirm the window is rendering something, not a design choice.
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render(); // delegates to the active Screen, once we have one
    }
}