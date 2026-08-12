package com.kitchenkaos.game;

import com.badlogic.gdx.Game;
import com.kitchenkaos.game.screens.ShiftScreen;

/**
 * Root of the game. Hands off immediately to ShiftScreen — Part 1 has
 * no menu/title screen yet, we go straight into a shift to test the loop.
 */
public class KitchenKaosGame extends Game {

    @Override
    public void create() {
        setScreen(new ShiftScreen());
    }
}