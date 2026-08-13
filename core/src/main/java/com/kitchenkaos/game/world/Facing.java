package com.kitchenkaos.game.world;

/** Which way the player is currently facing. Drives interaction eligibility. */
public enum Facing {
    UP(0f, 1f),
    DOWN(0f, -1f),
    LEFT(-1f, 0f),
    RIGHT(1f, 0f);

    public final float dx;
    public final float dy;

    Facing(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }
}