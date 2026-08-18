package com.kitchenkaos.game;

/**
 * Central tunable numbers pulled from the GDD, so nobody has to go
 * hunting through logic classes to tweak pacing. Keep this file boring
 * and dumb — constants only, no behavior.
 */
public final class GameConstants {

    // Private constructor — this class is never instantiated, it's
    // just a bag of static constants.
    private GameConstants() {}

    // --- Time compression (GDD §4): 12 real minutes == 12 game hours ---
    public static final float REAL_SECONDS_PER_GAME_HOUR = 60f;
    public static final float SHIFT_LENGTH_GAME_HOURS = 12f; // 9am -> 9pm
    public static final int SHIFT_START_GAME_HOUR = 9;       // 9am, for display

    // Key clock marks, expressed as hours-since-shift-start (0 = clock-in)
    public static final float PREP_DEADLINE_HOUR = 8f;       // 5pm
    public static final float DINNER_RUSH_START_HOUR = 8f;   // 5pm
    public static final float SCHEDULED_CLOSE_HOUR = 11f;    // 8pm
    public static final float CLOSING_WINDOW_END_HOUR = 12f; // 9pm
    // Anything at/beyond CLOSING_WINDOW_END_HOUR counts as overtime.

    // --- Flow Meter ---
    public static final float FLOW_MIN = 0f;
    public static final float FLOW_MAX = 100f;
    public static final float FLOW_START = 50f;
    public static final float FLOW_GOOD_TIMING_GAIN = 4f;
    public static final float FLOW_BAD_TIMING_LOSS = 7f;
    public static final float FLOW_MISTAKE_LOSS = 12f;
    // Below this threshold, mistakes get more likely / stations run slower.
    public static final float FLOW_RISK_THRESHOLD = 30f;
    // At/above this threshold, you get a speed/accuracy bonus ("in the zone").
    public static final float FLOW_BONUS_THRESHOLD = 75f;
    // --- Player interaction ---
    public static final float INTERACTION_RANGE = 70f;      // pixels
    public static final float FACING_DOT_THRESHOLD = 0.5f; // ~60-degree facing cone

    // --- Station hold/burn risk (heat-risk stations only, e.g. Grill/Sauté/Fryer) ---
    // How long a player-cooked dish can sit "ready" at a heat-risk station
    // before it burns and has to be remade from scratch. NPC-cooked dishes
    // never sit — NPCs auto-collect the instant their task finishes, since
    // that's already abstracted by the automation system.
    public static final float BURN_GRACE_SECONDS = 20f;
}