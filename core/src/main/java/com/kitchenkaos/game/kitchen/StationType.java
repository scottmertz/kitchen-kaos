package com.kitchenkaos.game.kitchen;

/**
 * Part 1 stations, per the GDD's Part 1 scope. Traits describe each
 * station's real-world failure modes, consumed by ProblemEvent logic,
 * Station's capacity, and now cleanliness. EXPO has no cleaning concept
 * (capacity 0, threshold 0) — it's not a cooking station.
 *
 * cleanThreshold values are placeholders based on real-world mess
 * buildup (Fryer splatters fastest, Cold barely dirties) — tune once
 * playtesting shows how it actually feels.
 */
public enum StationType {
    GRILL(true, false, false, 2, 5),
    SAUTE(true, false, false, 2, 6),
    FRYER(true, false, false, 2, 4),
    EXPO(false, false, false, 0, 0),
    PREP(false, false, true, 1, 8),
    COLD(false, true, false, 1, 10);

    public final boolean hasHeatRisk;
    public final boolean hasHoldTimeRisk;
    public final boolean hasSpoiledIngredientRisk;
    public final int baseCapacity;

    /** Slot-completions allowed before this station must be cleaned before cooking again. */
    public final int cleanThreshold;

    StationType(boolean hasHeatRisk, boolean hasHoldTimeRisk, boolean hasSpoiledIngredientRisk,
                int baseCapacity, int cleanThreshold) {
        this.hasHeatRisk = hasHeatRisk;
        this.hasHoldTimeRisk = hasHoldTimeRisk;
        this.hasSpoiledIngredientRisk = hasSpoiledIngredientRisk;
        this.baseCapacity = baseCapacity;
        this.cleanThreshold = cleanThreshold;
    }
}