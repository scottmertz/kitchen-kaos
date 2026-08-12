package com.kitchenkaos.game.orders;

import com.kitchenkaos.game.menu.Dish;

/**
 * One customer order. A ticket can contain multiple dishes (a table
 * ordering several things at once). Tracks when it came in (in game-
 * clock hours-since-start, matching TimeCompressionClock) so callers
 * can compute wait time later for the "long wait" problem event (GDD §12).
 */
public class Ticket {

    private final Dish[] dishes;
    private final float firedAtGameHour;
    private boolean[] dishCompleted;
    private boolean fulfilled = false;

    public Ticket(float firedAtGameHour, Dish... dishes) {
        this.firedAtGameHour = firedAtGameHour;
        this.dishes = dishes;
        this.dishCompleted = new boolean[dishes.length];
    }

    /** Marks one dish on this ticket done. Ticket auto-marks itself fulfilled once all are. */
    public void markDishComplete(int dishIndex) {
        dishCompleted[dishIndex] = true;
        fulfilled = true;
        for (boolean done : dishCompleted) {
            if (!done) {
                fulfilled = false;
                break;
            }
        }
    }

    public boolean isDishComplete(int dishIndex) {
        return dishCompleted[dishIndex];
    }

    public float getWaitSeconds(float currentGameHour, float realSecondsPerGameHour) {
        return (currentGameHour - firedAtGameHour) * realSecondsPerGameHour;
    }

    public Dish[] getDishes() {
        return dishes;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public float getFiredAtGameHour() {
        return firedAtGameHour;
    }
}