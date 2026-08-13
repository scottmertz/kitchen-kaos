package com.kitchenkaos.game.sim;

/**
 * Coarse work-eligibility gate for the shift — separate from ShiftPhase
 * (which is a finer-grained, time-based label like "Lunch Rush" used for
 * pacing once the clock is already running). Movement is ALWAYS allowed
 * regardless of this value; everything else (clock ticking, tickets
 * spawning, station interaction) gates off it.
 */
public enum ShiftState {
    NOT_CLOCKED_IN,
    PREPPING,
    OPEN,
    CLOSING,
    CLOCKED_OUT
}