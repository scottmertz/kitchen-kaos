package com.kitchenkaos.game.kitchen;

import com.kitchenkaos.game.GameConstants;
import com.kitchenkaos.game.orders.Ticket;
import com.kitchenkaos.game.sim.FlowMeter;

import java.util.ArrayList;
import java.util.List;

/**
 * A physical station with one or more concurrent cook slots (e.g. a
 * flat-top grill cooking 2 burgers at once). Each slot tracks its own
 * busy/duration/elapsed state independently, PLUS a real reference to
 * which ticket/dish it's cooking and whether the player or an NPC
 * started it — replaces the earlier "guess which dish just finished"
 * approximation that used to live in ShiftScreen.
 *
 * Heat-risk stations (Grill/Sauté/Fryer) add a real consequence for
 * player-cooked dishes: finishing doesn't auto-collect. It goes
 * "ready" and sits there — walk back and collect it in time, or it
 * burns and has to be remade. NPC-worked slots skip this entirely and
 * auto-collect instantly, since NPC "walking over to grab it" is
 * already abstracted by the automation system (GDD §6/8a).
 */
public class Station {

    private final StationType type;
    private final List<CookSlot> slots = new ArrayList<>();

    private static class CookSlot {
        boolean busy = false;
        float taskDurationSeconds = 0f;
        float taskElapsedSeconds = 0f;

        // Real identity of what's cooking — replaces the old "guess from
        // activeTickets" approximation.
        Ticket ticketRef = null;
        int dishIndex = -1;
        boolean workedByPlayer = false;

        // Only meaningful at heat-risk stations for player-worked slots.
        boolean ready = false;
        float readyElapsedSeconds = 0f;
    }

    /** One thing that happened to a slot this frame, for the caller to react to. */
    public static class SlotEvent {
        public enum Type { FINISHED, BURNED }

        public final Type type;
        public final Ticket ticket;
        public final int dishIndex;
        public final boolean workedByPlayer;

        SlotEvent(Type type, Ticket ticket, int dishIndex, boolean workedByPlayer) {
            this.type = type;
            this.ticket = ticket;
            this.dishIndex = dishIndex;
            this.workedByPlayer = workedByPlayer;
        }
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

    /** Player-driven cook start. Ties the slot to a real ticket/dish. */
    public boolean startTask(Ticket ticket, int dishIndex, float baseDurationSeconds, FlowMeter flow) {
        return startTask(ticket, dishIndex, true, baseDurationSeconds, flow.getSpeedMultiplier());
    }

    /** NPC-driven cook start — same idea, but skill-based speed instead of Flow's. */
    public boolean startTask(Ticket ticket, int dishIndex, float baseDurationSeconds, float npcSpeedMultiplier) {
        return startTask(ticket, dishIndex, false, baseDurationSeconds, npcSpeedMultiplier);
    }

    private boolean startTask(Ticket ticket, int dishIndex, boolean workedByPlayer,
                              float baseDurationSeconds, float speedMultiplier) {
        if (needsCleaning()) {
            return false;
        }
        for (CookSlot slot : slots) {
            if (!slot.busy && !slot.ready) {
                slot.busy = true;
                slot.taskElapsedSeconds = 0f;
                slot.taskDurationSeconds = baseDurationSeconds / speedMultiplier;
                slot.ticketRef = ticket;
                slot.dishIndex = dishIndex;
                slot.workedByPlayer = workedByPlayer;
                return true;
            }
        }
        return false;
    }

    /**
     * Advances every slot. Returns whatever finished or burned THIS
     * frame — usually empty. Callers should process each event once;
     * more than one can legitimately happen on the same frame.
     */
    public List<SlotEvent> update(float deltaSeconds) {
        List<SlotEvent> events = new ArrayList<>();

        for (CookSlot slot : slots) {
            if (slot.busy) {
                slot.taskElapsedSeconds += deltaSeconds;
                if (slot.taskElapsedSeconds >= slot.taskDurationSeconds) {
                    slot.busy = false;
                    completionsSinceClean++;

                    boolean atRiskOfBurning = type.hasHeatRisk && slot.workedByPlayer;
                    if (atRiskOfBurning) {
                        // Goes "ready" instead of auto-finishing — must be
                        // collected in time or it burns. Slot stays occupied
                        // (not free for a new task) until collected/burned.
                        slot.ready = true;
                        slot.readyElapsedSeconds = 0f;
                    } else {
                        events.add(new SlotEvent(
                                SlotEvent.Type.FINISHED, slot.ticketRef, slot.dishIndex, slot.workedByPlayer));
                        clearSlot(slot);
                    }
                }
            } else if (slot.ready) {
                slot.readyElapsedSeconds += deltaSeconds;
                if (slot.readyElapsedSeconds >= GameConstants.BURN_GRACE_SECONDS) {
                    events.add(new SlotEvent(
                            SlotEvent.Type.BURNED, slot.ticketRef, slot.dishIndex, slot.workedByPlayer));
                    clearSlot(slot);
                }
            }
        }

        if (cleaning) {
            cleanElapsedSeconds += deltaSeconds;
            if (cleanElapsedSeconds >= CLEAN_DURATION_SECONDS) {
                cleaning = false;
                completionsSinceClean = 0;
            }
        }

        return events;
    }

    /**
     * Player action: collect a ready dish before it burns. Returns the
     * FINISHED event to process (mistake roll doesn't apply — only
     * NPC-worked slots roll mistakes), or null if this slot isn't
     * actually ready to collect.
     */
    public SlotEvent collect(int slotIndex) {
        CookSlot slot = slots.get(slotIndex);
        if (!slot.ready) {
            return null;
        }
        SlotEvent event = new SlotEvent(SlotEvent.Type.FINISHED, slot.ticketRef, slot.dishIndex, slot.workedByPlayer);
        clearSlot(slot);
        return event;
    }

    private void clearSlot(CookSlot slot) {
        slot.busy = false;
        slot.ready = false;
        slot.readyElapsedSeconds = 0f;
        slot.ticketRef = null;
        slot.dishIndex = -1;
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
            if (!slot.busy && !slot.ready) {
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

    public boolean isSlotReady(int index) {
        return slots.get(index).ready;
    }

    public String getSlotDishName(int index) {
        CookSlot slot = slots.get(index);
        if (slot.ticketRef == null || slot.dishIndex < 0) {
            return null;
        }
        return slot.ticketRef.getDishes()[slot.dishIndex].getName();
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