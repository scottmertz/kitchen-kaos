package com.kitchenkaos.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.kitchenkaos.game.kitchen.Station;

/**
 * The world-space representation of a Station — position, size, and
 * solid collision bounds. Station itself (in the kitchen package) stays
 * pure simulation logic with no idea it exists anywhere in space; this
 * class is the bridge between "a station that cooks things" and "a
 * physical object you can walk up to." interact() is a stub for now —
 * real station UI (ingredients list, start cooking) is a later step.
 */
public class WorldStation implements Interactable {

    private final Station station;
    private final Rectangle bounds;

    public WorldStation(Station station, float x, float y, float width, float height) {
        this.station = station;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Station getStation() {
        return station;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public String getLabel() {
        return station.getType() + " Station";
    }

    @Override
    public void interact() {
        // Placeholder — logs to console so we can PROVE interaction
        // fired correctly before any real station UI exists.
        Gdx.app.log("Interact", "Interacted with " + station.getType());
    }
}