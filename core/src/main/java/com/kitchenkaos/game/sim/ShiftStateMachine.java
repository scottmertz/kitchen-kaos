package com.kitchenkaos.game.sim;

/**
 * Drives ShiftState transitions. Clock-in/clock-out are explicit player
 * actions (clockIn()/clockOut()) — the real trigger for these is the
 * POS,so ShiftScreen will call PREPPING -> OPEN -> CLOSING
 * are automatic and time-based, matching the fixed shift schedule
 * (GDD §4) — the doors open on schedule whether or not prep is done,
 * same as a real restaurant.
 */
public class ShiftStateMachine {

    private ShiftState state = ShiftState.NOT_CLOCKED_IN;

    /** Call once per frame with the clock's current phase — but only while clocked in. */
    public void update(ShiftPhase currentPhase) {
        if (state == ShiftState.NOT_CLOCKED_IN || state == ShiftState.CLOCKED_OUT) {
            return; // no automatic transitions possible outside a working state
        }

        if (currentPhase == ShiftPhase.CLOSING || currentPhase == ShiftPhase.OVERTIME) {
            state = ShiftState.CLOSING;
        } else if (currentPhase == ShiftPhase.CLOCK_IN || currentPhase == ShiftPhase.PREP_OPEN) {
            state = ShiftState.PREPPING;
        } else {
            // SOLO_OPEN through DINNER_RUSH
            state = ShiftState.OPEN;
        }
    }

    /** Real trigger is the POS (Step 4). Starts the clock ticking. */
    public void clockIn() {
        if (state == ShiftState.NOT_CLOCKED_IN) {
            state = ShiftState.PREPPING;
        }
    }

    /**
     * Real trigger is the POS. Loosened from an earlier CLOSING-only
     * restriction — the full early/late clock-out consequence system
     * (pay/Fatigue/Owner Happiness tradeoffs) is a deferred, dedicated
     * feature; until it exists, the ACTION itself shouldn't be blocked.
     */
    public void clockOut() {
        if (isClockedIn()) {
            state = ShiftState.CLOCKED_OUT;
        }
    }

    public ShiftState getState() {
        return state;
    }

    public boolean isClockedIn() {
        return state != ShiftState.NOT_CLOCKED_IN && state != ShiftState.CLOCKED_OUT;
    }

    public boolean isOpenForCustomers() {
        return state == ShiftState.OPEN;
    }
}