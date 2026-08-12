package com.kitchenkaos.game.menu;

import com.kitchenkaos.game.kitchen.StationType;

/**
 * A single menu item. Immutable — once a Dish is defined it doesn't
 * change at runtime (menu unlocks/changes would create/swap Dish
 * instances, not mutate one). baseCookSeconds is a placeholder value
 * for now; real pacing numbers come from playtesting, not guesswork.
 */
public class Dish {

    private final String name;
    private final StationType[] stations;
    private final float baseCookSeconds;

    public Dish(String name, float baseCookSeconds, StationType... stations) {
        this.name = name;
        this.baseCookSeconds = baseCookSeconds;
        this.stations = stations;
    }

    public String getName() {
        return name;
    }

    /** Which station(s) this dish can be worked at, in listed order (GDD §9.1). */
    public StationType[] getStations() {
        return stations;
    }

    /** The FIRST/primary station — most dishes list one station first even if multi-station. */
    public StationType getPrimaryStation() {
        return stations[0];
    }

    public float getBaseCookSeconds() {
        return baseCookSeconds;
    }
}