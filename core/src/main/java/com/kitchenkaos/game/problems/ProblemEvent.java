package com.kitchenkaos.game.problems;

/**
 * A single problem instance that occurred during the shift. This class
 * is deliberately dumb — it just records WHAT happened and WHEN, in
 * game-hours (matching TimeCompressionClock). Deciding when to CREATE
 * one is the job of whatever's watching Stations/Tickets each frame
 * (that wiring happens in ShiftScreen, Step 18) — ProblemEvent itself
 * has no logic, no triggering conditions, nothing to update().
 */
public class ProblemEvent {

    private final ProblemType type;
    private final float occurredAtGameHour;
    private final String detail;

    public ProblemEvent(ProblemType type, float occurredAtGameHour, String detail) {
        this.type = type;
        this.occurredAtGameHour = occurredAtGameHour;
        this.detail = detail;
    }

    public ProblemType getType() {
        return type;
    }

    public float getOccurredAtGameHour() {
        return occurredAtGameHour;
    }

    /** Short human-readable context, e.g. "Buffalo Wings — Fryer" or "Table waited 6 min". */
    public String getDetail() {
        return detail;
    }
}