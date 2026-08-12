package com.kitchenkaos.game.problems;

/**
 * Part 1 scope only (GDD §28): the 3 basic Tier 1 problems needed to
 * test if pressure feels good. The other 8 Tier 1 problems in GDD §12
 * (wrong order, printer jam, spilled plate, etc.) are real but
 * deliberately out of scope until a later pass.
 */
public enum ProblemType {
    /** Ingredient ran out — lost sale, NOT a mistake (GDD §9/§13 distinction). */
    EIGHTY_SIXED_INGREDIENT,

    /** Food left too long at a heat-risk station — remade, costs time/ingredients. */
    BURNED_FOOD,

    /** Ticket sitting unfulfilled too long — drains customer patience. */
    LONG_WAIT
}