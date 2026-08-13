package com.kitchenkaos.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.kitchenkaos.game.pos.PosMenu;

/**
 * The physical POS terminal. Walking up, facing it, and pressing SPACE
 * opens its menu (same Interactable contract as WorldStation) — from
 * there, ShiftScreen switches input handling into "menu mode" until
 * the player exits.
 */
public class WorldPOS implements Interactable {

    private final PosMenu menu;
    private final Rectangle bounds;

    public WorldPOS(PosMenu menu, float x, float y, float width, float height) {
        this.menu = menu;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public PosMenu getMenu() {
        return menu;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public String getLabel() {
        return "POS Terminal";
    }

    @Override
    public void interact() {
        menu.open();
    }
}