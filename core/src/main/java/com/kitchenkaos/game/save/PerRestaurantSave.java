package com.kitchenkaos.game.save;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything location-bound — wiped/replaced when the player leaves a
 * restaurant. Field list is grounded in exactly what's real in the
 * codebase today (§28 audit, 2026-08-18) — no fields for wallets,
 * vendors, reputation, etc. until those systems actually exist.
 *
 * Plain public-field DTOs throughout — see PersistentChefProfile for
 * why (libGDX Json serializes these directly).
 */
public class PerRestaurantSave {

    public static final int CURRENT_VERSION = 1;

    public int saveVersion = CURRENT_VERSION;

    // --- Shift state ---
    public String shiftState;           // ShiftState enum name
    public float elapsedRealSeconds;    // TimeCompressionClock's one field
    public float flowValue;             // FlowMeter's one field

    // --- Ticket lifecycle ---
    public int nextTicketId;                     // Ticket.nextId, so post-load spawns never collide
    public float ticketSpawnerSecondsSinceLastSpawn;
    public List<TicketSave> activeTickets = new ArrayList<>();
    public List<Integer> alreadyFlaggedLongWaitTicketIds = new ArrayList<>();

    // --- Stations ---
    public List<StationSave> stations = new ArrayList<>();

    // --- Problem log ---
    public List<ProblemEventSave> problemLog = new ArrayList<>();

    // --- Staff ---
    public List<NpcCookSave> roster = new ArrayList<>();

    // --- Player ---
    public float playerX;
    public float playerY;
    public String playerFacing; // Facing enum name

    // --- Nested DTOs ---

    public static class TicketSave {
        public int id;
        public float firedAtGameHour;
        public List<String> dishNames = new ArrayList<>();   // resolved against Tier1Menu.ALL by name
        public List<Boolean> dishCompleted = new ArrayList<>();
        public boolean submitted;
    }

    public static class StationSave {
        public String stationType; // StationType enum name
        public int completionsSinceClean;
        public boolean cleaning;
        public float cleanElapsedSeconds;
        public List<SlotSave> slots = new ArrayList<>();
    }

    public static class SlotSave {
        public boolean busy;
        public boolean ready;
        public float taskElapsedSeconds;
        public float taskDurationSeconds;
        public float readyElapsedSeconds;
        public boolean workedByPlayer;
        public Integer ticketId; // null if idle
        public int dishIndex = -1;
    }

    public static class ProblemEventSave {
        public String problemType; // ProblemType enum name
        public float gameHour;
        public String description;
    }

    public static class NpcCookSave {
        public String name;
        // StationType enum name -> skill value
        public java.util.Map<String, Float> skillByStation = new java.util.HashMap<>();
        public List<String> assignedStations = new ArrayList<>(); // StationType enum names
    }
}