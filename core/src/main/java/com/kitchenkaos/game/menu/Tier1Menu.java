package com.kitchenkaos.game.menu;

import com.kitchenkaos.game.kitchen.StationType;

/**
 * The 10 Tier 1 "Starter (Bar & Tavern)" dishes from GDD §9.1 — the
 * only menu Part 1 needs, per the Part 1 to-do list (§28).
 *
 * baseCookSeconds values below are PLACEHOLDERS — round numbers picked
 * to feel roughly plausible (fryer items quick, fish & chips slower).
 * These are the first thing to retune once you're actually playtesting
 * pacing — don't treat them as final.
 *
 * Where the GDD lists "House Side Salad — Prep/Cold", COLD isn't a
 * StationType we've defined yet (no walk-in/cold-station mechanic built
 * in Part 1), so it maps to PREP only for now. Worth revisiting if a
 * dedicated cold-station mechanic gets added later.
 */
public final class Tier1Menu {

    private Tier1Menu() {}

    public static final Dish CLASSIC_CHEESEBURGER =
            new Dish("Classic Cheeseburger", 12f, StationType.GRILL);

    public static final Dish LOADED_FRIES =
            new Dish("Loaded Fries", 8f, StationType.FRYER);

    public static final Dish FRIED_MOZZARELLA_STICKS =
            new Dish("Fried Mozzarella Sticks", 7f, StationType.FRYER);

    public static final Dish BUFFALO_WINGS =
            new Dish("Buffalo Wings", 10f, StationType.FRYER);

    public static final Dish GRILLED_CHEESE_TOMATO_SOUP =
            new Dish("Grilled Cheese & Tomato Soup", 9f, StationType.GRILL, StationType.SAUTE);

    public static final Dish CHICKEN_TENDERS_FRIES =
            new Dish("Chicken Tenders & Fries", 11f, StationType.FRYER);

    public static final Dish BLT_SANDWICH =
            new Dish("BLT Sandwich", 8f, StationType.GRILL);

    public static final Dish HOUSE_SIDE_SALAD =
            new Dish("House Side Salad", 6f, StationType.COLD);

    public static final Dish BEER_BATTERED_FISH_CHIPS =
            new Dish("Beer-Battered Fish & Chips", 14f, StationType.FRYER);

    public static final Dish CHILI_CHEESE_NACHOS =
            new Dish("Chili Cheese Nachos", 9f, StationType.FRYER, StationType.SAUTE);

        /** All 11 Tier 1 dishes, in GDD §9.1 order. */
    public static final Dish[] ALL = {
            CLASSIC_CHEESEBURGER,
            LOADED_FRIES,
            FRIED_MOZZARELLA_STICKS,
            BUFFALO_WINGS,
            GRILLED_CHEESE_TOMATO_SOUP,
            CHICKEN_TENDERS_FRIES,
            BLT_SANDWICH,
            HOUSE_SIDE_SALAD,
            BEER_BATTERED_FISH_CHIPS,
            CHILI_CHEESE_NACHOS,
    };
}