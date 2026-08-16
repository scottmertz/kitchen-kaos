package com.kitchenkaos.game.kitchen;

/**
 * Part 1 stations, per the GDD's Part 1 scope. Each station carries
 * traits describing its real-world failure mode AND its base cooking
 * capacity — these get consumed by ProblemEvent logic and Station
 * itself, respectively. EXPO's capacity is unused/meaningless: it's
 * not a cooking station, it's the final "send the ticket" checkpoint
 * (see WorldExpo).
 */
public enum StationType {
    GRILL(true, false, false, 2),
    SAUTE(true, false, false, 2),
    FRYER(true, false, false, 2),
    EXPO(false, false, false, 0),
    PREP(false, false, true, 1),
    COLD(false, true, false, 1);

    public final boolean hasHeatRisk;
    public final boolean hasHoldTimeRisk;
    public final boolean hasSpoiledIngredientRisk;

    /** Concurrent cook slots at Part 1's starting equipment tier. Increasing this later is a Kitchen Upgrades (§8) concern, not yet built. */
    public final int baseCapacity;

    StationType(boolean hasHeatRisk, boolean hasHoldTimeRisk, boolean hasSpoiledIngredientRisk, int baseCapacity) {
        this.hasHeatRisk = hasHeatRisk;
        this.hasHoldTimeRisk = hasHoldTimeRisk;
        this.hasSpoiledIngredientRisk = hasSpoiledIngredientRisk;
        this.baseCapacity = baseCapacity;
    }
}