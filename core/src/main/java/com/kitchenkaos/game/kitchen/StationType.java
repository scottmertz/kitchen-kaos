package com.kitchenkaos.game.kitchen;

/**
 * Part 1 stations only, per the GDD's Part 1 scope. More station types
 * (if any get added in later parts) would extend this enum — nothing
 * elsewhere should hardcode "these are the only 5 stations that will
 * ever exist."
 */
public enum StationType {
    GRILL,
    SAUTE,
    FRYER,
    EXPO,
    PREP
}