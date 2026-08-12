package com.kitchenkaos.game.orders;

import com.badlogic.gdx.math.MathUtils;
import com.kitchenkaos.game.menu.Dish;
import com.kitchenkaos.game.menu.Tier1Menu;
import com.kitchenkaos.game.sim.ShiftPhase;

/**
 * Generates new Tickets over time. Spawn RATE is driven by ShiftPhase —
 * lunch rush and dinner rush spawn faster than the pre-dinner lull,
 * matching the shift structure in GDD §4. Dish selection is random
 * from the Tier 1 menu for now; smarter selection (avoiding repeats,
 * weighting by popularity) can come later.
 */
public class TicketSpawner {

    private float secondsSinceLastSpawn = 0f;

    /**
     * Call once per frame. currentGameHour and currentPhase come from
     * TimeCompressionClock, so spawn pacing always matches the actual
     * shift clock rather than tracking a separate notion of time.
     * Returns a new Ticket the frame one spawns, otherwise null.
     */
    public Ticket update(float deltaSeconds, float currentGameHour, ShiftPhase currentPhase) {
        secondsSinceLastSpawn += deltaSeconds;

        float intervalSeconds = spawnIntervalFor(currentPhase);
        if (secondsSinceLastSpawn >= intervalSeconds) {
            secondsSinceLastSpawn = 0f;
            return spawnTicket(currentGameHour);
        }
        return null;
    }

    /** Average real-seconds between ticket spawns during a given phase. */
    private float spawnIntervalFor(ShiftPhase phase) {
        switch (phase) {
            case LUNCH_RUSH:
            case DINNER_RUSH:
                return 8f;
            case FULL_KITCHEN:
                return 12f;
            case SOLO_OPEN:
            case PRE_DINNER_LULL:
                return 20f;
            case CLOSING:
                return 30f;
            default:
                // CLOCK_IN, PREP_OPEN, OVERTIME — no walk-in customers expected
                return Float.MAX_VALUE;
        }
    }

    private Ticket spawnTicket(float currentGameHour) {
        // 1-2 dishes per ticket for now — a party-size system can replace
        // this later without changing Ticket's shape.
        int dishCount = MathUtils.randomBoolean(0.7f) ? 1 : 2;
        Dish[] dishes = new Dish[dishCount];
        for (int i = 0; i < dishCount; i++) {
            dishes[i] = Tier1Menu.ALL[MathUtils.random(Tier1Menu.ALL.length - 1)];
        }
        return new Ticket(currentGameHour, dishes);
    }
}