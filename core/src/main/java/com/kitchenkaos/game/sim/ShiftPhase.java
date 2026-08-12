package com.kitchenkaos.game.sim;

/**
 * Mirrors the shift structure table from GDD §4. startHour/endHour are
 * expressed as hours-since-clock-in (0 = 9am). Given a point in the
 * shift, forHour() tells you which phase you're in — this is what
 * drives staffing changes, ticket pacing, and UI labeling later.
 */
public enum ShiftPhase {

    CLOCK_IN(0f, 1f, "Clock In"),
    PREP_OPEN(1f, 2f, "Prep Begins"),
    SOLO_OPEN(2f, 3f, "Open — Solo"),
    LUNCH_RUSH(3f, 6f, "Lunch Rush"),
    PRE_DINNER_LULL(6f, 7f, "Pre-Dinner Lull"),
    FULL_KITCHEN(7f, 8f, "Full 3-Person Kitchen"),
    DINNER_RUSH(8f, 11f, "Dinner Rush"),
    CLOSING(11f, 12f, "Closing / Cleaning"),
    OVERTIME(12f, Float.MAX_VALUE, "Overtime");

    public final float startHour;
    public final float endHour;
    public final String label;

    ShiftPhase(float startHour, float endHour, String label) {
        this.startHour = startHour;
        this.endHour = endHour;
        this.label = label;
    }

    /**
     * Given hours-since-shift-start, returns which phase that falls in.
     * Falls through to OVERTIME if somehow past every defined range.
     */
    public static ShiftPhase forHour(float hoursSinceStart) {
        for (ShiftPhase phase : values()) {
            if (hoursSinceStart >= phase.startHour && hoursSinceStart < phase.endHour) {
                return phase;
            }
        }
        return OVERTIME;
    }
}