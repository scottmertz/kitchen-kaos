package com.kitchenkaos.game.kitchen;

import com.kitchenkaos.game.sim.FlowMeter;

/**
 * One physical station in the kitchen. Tracks its type, whether it's
 * currently working on something, and how far along that task is.
 * Station doesn't know what DISH it's cooking yet — that comes later
 * once Dish/Ticket exist. For now this is just the "is it busy, how
 * far along" state machine, so we can test the timing feel in isolation.
 */
public class Station {

    private final StationType type;

    private boolean busy = false;
    private float taskDurationSeconds = 0f;
    private float taskElapsedSeconds = 0f;

    public Station(StationType type) {
        this.type = type;
    }

    /**
     * Begins a task at this station. baseDurationSeconds is how long the
     * task takes at flow=neutral (1.0x speed); the actual time it takes
     * gets adjusted by the FlowMeter's current multiplier.
     */
    public void startTask(float baseDurationSeconds, FlowMeter flow) {
        this.busy = true;
        this.taskElapsedSeconds = 0f;
        // Higher speed multiplier = task takes LESS time, so we divide
        // rather than multiply.
        this.taskDurationSeconds = baseDurationSeconds / flow.getSpeedMultiplier();
    }

    /**
     * Advances the current task. Returns true the frame the task
     * completes (so callers know to trigger "dish ready" logic exactly
     * once, not every frame afterward).
     */
    public boolean update(float deltaSeconds) {
        if (!busy) {
            return false;
        }
        taskElapsedSeconds += deltaSeconds;
        if (taskElapsedSeconds >= taskDurationSeconds) {
            busy = false;
            return true;
        }
        return false;
    }

    /** 0.0 (just started) to 1.0 (done) — useful for a progress bar later. */
    public float getProgress() {
        if (!busy || taskDurationSeconds <= 0f) {
            return 0f;
        }
        return Math.min(taskElapsedSeconds / taskDurationSeconds, 1f);
    }

    public boolean isBusy() {
        return busy;
    }

    public StationType getType() {
        return type;
    }
}