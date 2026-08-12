package com.kitchenkaos.game.sim;

import com.kitchenkaos.game.GameConstants;

/**
 * Drives the 12-real-minute == 12-game-hour shift clock (GDD §4).
 * Call update() once per frame with libGDX's delta time (seconds since
 * last frame). Everything else (ShiftScreen, staffing, ticket pacing)
 * reads the current state from this class rather than tracking its own
 * separate notion of "what time it is."
 */
public class TimeCompressionClock {

    private float elapsedRealSeconds = 0f;

    // These two flags exist so other systems can ask "did we JUST cross
    // this line" (a one-time event, like a phase-change notification)
    // instead of every system having to do >= comparisons themselves.
    private boolean pastPrepDeadline = false;
    private boolean pastScheduledClose = false;

    public void update(float deltaSeconds) {
        elapsedRealSeconds += deltaSeconds;

        if (!pastPrepDeadline && getHoursSinceStart() >= GameConstants.PREP_DEADLINE_HOUR) {
            pastPrepDeadline = true;
        }
        if (!pastScheduledClose && getHoursSinceStart() >= GameConstants.SCHEDULED_CLOSE_HOUR) {
            pastScheduledClose = true;
        }
    }

    /** Game-hours elapsed since clock-in (0 = 9am, fractional between marks). */
    public float getHoursSinceStart() {
        return elapsedRealSeconds / GameConstants.REAL_SECONDS_PER_GAME_HOUR;
    }

    /** In-game wall-clock hour (24h format) for display purposes, e.g. UI text. */
    public int getDisplayHour24() {
        int hour = GameConstants.SHIFT_START_GAME_HOUR + (int) getHoursSinceStart();
        return hour % 24;
    }

    public ShiftPhase getPhase() {
        return ShiftPhase.forHour(getHoursSinceStart());
    }

    public boolean isPastPrepDeadline() {
        return pastPrepDeadline;
    }

    public boolean isPastScheduledClose() {
        return pastScheduledClose;
    }

    public boolean isInOvertime() {
        return getHoursSinceStart() >= GameConstants.CLOSING_WINDOW_END_HOUR;
    }
}