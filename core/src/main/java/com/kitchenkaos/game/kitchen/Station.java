package com.kitchenkaos.game.kitchen;

import com.kitchenkaos.game.sim.FlowMeter;

import java.util.ArrayList;
import java.util.List;

/**
 * A physical station with one or more concurrent cook slots (e.g. a
 * flat-top grill cooking 2 burgers at once). Each slot tracks its own
 * busy/duration/elapsed state independently — this replaced Station's
 * original single-task design once capacity became a real mechanic.
 */
public class Station {

    private final StationType type;
    private final List<CookSlot> slots = new ArrayList<>();

    private static class CookSlot {
        boolean busy = false;
        float taskDurationSeconds = 0f;
        float taskElapsedSeconds = 0f;
    }

    private static final float CLEAN_DURATION_SECONDS = 1f;

    private int completionsSinceClean = 0;
    private boolean cleaning = false;
    private float cleanElapsedSeconds = 0f;

    public Station(StationType type) {
        this.type = type;
        // EXPO's baseCapacity is 0 (it's not a cooking station at all) —
        // clamp to 1 so this class never has a zero-length, unusable list.
        int capacity = Math.max(type.baseCapacity, 1);
        for (int i = 0; i < capacity; i++) {
            slots.add(new CookSlot());
        }
    }

    /** Starts a task in the first free slot. Returns false if every slot is currently busy. */
    public boolean startTask(float baseDurationSeconds, FlowMeter flow) {
        return startTask(baseDurationSeconds, flow.getSpeedMultiplier());
    }

    /** Used by NPC-driven tasks, which apply the NPC's own skill-based speed instead of Flow's. */
    public boolean startTask(float baseDurationSeconds, float speedMultiplier) {
        if (needsCleaning()) {
            return false;
        }
        for (CookSlot slot : slots) {
            if (!slot.busy) {
                slot.busy = true;
                slot.taskElapsedSeconds = 0f;
                slot.taskDurationSeconds = baseDurationSeconds / speedMultiplier;
                return true;
            }
        }
        return false;
    }

    /**
     * Advances every slot. Returns how many slots completed THIS frame
     * (usually 0) — callers should invoke their completion handler once
     * per count, since more than one slot can finish on the same frame.
     */
    public int update(float deltaSeconds) {
        int finishedCount = 0;
        for (CookSlot slot : slots) {
            if (!slot.busy) {
                continue;
            }
            slot.taskElapsedSeconds += deltaSeconds;
            if (slot.taskElapsedSeconds >= slot.taskDurationSeconds) {
                slot.busy = false;
                finishedCount++;
                completionsSinceClean++;
            }
        }

        if (cleaning) {
            cleanElapsedSeconds += deltaSeconds;
            if (cleanElapsedSeconds >= CLEAN_DURATION_SECONDS) {
                cleaning = false;
                completionsSinceClean = 0;
            }
        }

        return finishedCount;
    }

    public boolean needsCleaning() {
        return type.cleanThreshold > 0 && completionsSinceClean >= type.cleanThreshold;
    }

    public boolean isCleaning() {
        return cleaning;
    }

    /** Begins the 1-second clean action. No-op if already cleaning or not actually dirty. */
    public void startCleaning() {
        if (!cleaning && needsCleaning()) {
            cleaning = true;
            cleanElapsedSeconds = 0f;
        }
    }

    /** NPC auto-clean — instant, unlike the player's 1-second startCleaning() action. */
    public void autoClean() {
        if (needsCleaning()) {
            completionsSinceClean = 0;
        }
    }

    public int getCompletionsSinceClean() {
        return completionsSinceClean;
    }

    public boolean hasFreeSlot() {
        for (CookSlot slot : slots) {
            if (!slot.busy) {
                return true;
            }
        }
        return false;
    }

    public int getSlotCount() {
        return slots.size();
    }

    public int getBusySlotCount() {
        int count = 0;
        for (CookSlot slot : slots) {
            if (slot.busy) {
                count++;
            }
        }
        return count;
    }

    public boolean isSlotBusy(int index) {
        return slots.get(index).busy;
    }

    public float getSlotProgress(int index) {
        CookSlot slot = slots.get(index);
        if (!slot.busy || slot.taskDurationSeconds <= 0f) {
            return 0f;
        }
        return Math.min(slot.taskElapsedSeconds / slot.taskDurationSeconds, 1f);
    }

    public StationType getType() {
        return type;
    }
}