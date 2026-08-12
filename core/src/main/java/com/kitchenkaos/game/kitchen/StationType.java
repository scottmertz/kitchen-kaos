package com.kitchenkaos.game.kitchen;

/**
 * Part 1 stations, per the GDD's Part 1 scope. Each station carries
 * traits describing its real-world failure mode — these get consumed
 * later by ProblemEvent logic, not by Station itself. Station stays a
 * dumb timer; StationType is where "what kind of thing can go wrong
 * here" lives.
 */
public enum StationType {
    GRILL(true, false, false),
    SAUTE(true, false, false),
    FRYER(true, false, false),
    EXPO(false, false, false),
    PREP(false, false, true),
    COLD(false, true, false);

    /** Can food burn/overcook here if left too long? */
    public final boolean hasHeatRisk;

    /** Does food held here risk spoiling/losing temp if left too long? */
    public final boolean hasHoldTimeRisk;

    /**
     * Can a dish routed here fail before cooking even starts, because
     * the raw ingredient itself arrived bad/86'd/short from a vendor?
     * This is a supply-chain risk, not a station-handling risk — it's
     * "the kitchen got dealt a bad hand," not "you left it too long."
     */
    public final boolean hasSpoiledIngredientRisk;

    StationType(boolean hasHeatRisk, boolean hasHoldTimeRisk, boolean hasSpoiledIngredientRisk) {
        this.hasHeatRisk = hasHeatRisk;
        this.hasHoldTimeRisk = hasHoldTimeRisk;
        this.hasSpoiledIngredientRisk = hasSpoiledIngredientRisk;
    }
}