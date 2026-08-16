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
        for (CookSlot slot : slots) {
            if (!slot.busy) {
                slot.busy = true;
                slot.taskElapsedSeconds = 0f;
                slot.taskDurationSeconds = baseDurationSeconds / flow.getSpeedMultiplier();
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
            }
        }
        return finishedCount;
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