package com.kitchenkaos.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

/**
 * The player character's position in world space. Free-roam movement —
 * not tile/grid-snapped. Movement is normalized so pressing two arrow
 * keys at once (diagonal) doesn't move faster than a single direction.
 */
public class Player {

    private static final float SPEED = 220f; // pixels per second

    private final Vector2 position;

    public Player(float startX, float startY) {
        position = new Vector2(startX, startY);
    }

    public void update(float deltaSeconds) {
        Vector2 direction = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  direction.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    direction.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  direction.y -= 1;

        if (!direction.isZero()) {
            direction.nor();
            position.mulAdd(direction, SPEED * deltaSeconds);
        }

        // Keep the player inside the world bounds — this IS the wall, for now.
        // Real wall/collision geometry comes later once a floor plan exists;
        // this just stops you walking into the void.
        position.x = com.badlogic.gdx.math.MathUtils.clamp(
                position.x, 0f, RestaurantWorld.WIDTH);
        position.y = com.badlogic.gdx.math.MathUtils.clamp(
                position.y, 0f, RestaurantWorld.HEIGHT);
    }

    public Vector2 getPosition() {
        return position;
    }
}