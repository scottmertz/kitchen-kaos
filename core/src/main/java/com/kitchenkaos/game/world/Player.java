package com.kitchenkaos.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * The player character. Tracks position, facing direction, and a solid
 * collision box. Movement resolves per-axis (move X, check collision,
 * revert if blocked; then move Y, check, revert if blocked) so the
 * player can slide along an edge instead of getting fully stuck when
 * approaching a solid object at an angle.
 */
public class Player {

    private static final float SPEED = 220f; // pixels per second
    private static final float SIZE = 32f;   // width/height of the collision box

    private final Vector2 position;
    private final Rectangle bounds;
    private Facing facing = Facing.DOWN;

    public Player(float startX, float startY) {
        position = new Vector2(startX, startY);
        bounds = new Rectangle(startX - SIZE / 2f, startY - SIZE / 2f, SIZE, SIZE);
    }

    public void update(float deltaSeconds, List<Rectangle> solidBounds) {
        Vector2 direction = new Vector2();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  direction.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) direction.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    direction.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  direction.y -= 1;

        if (direction.isZero()) {
            return; // no input this frame — facing stays as it was
        }

        direction.nor();
        updateFacing(direction);

        float moveX = direction.x * SPEED * deltaSeconds;
        float moveY = direction.y * SPEED * deltaSeconds;

        // Move on X, check collision, revert ONLY the X move if blocked.
        position.x += moveX;
        bounds.x = position.x - SIZE / 2f;
        if (collidesAny(solidBounds)) {
            position.x -= moveX;
            bounds.x = position.x - SIZE / 2f;
        }

        // Then move on Y, independently. This separation is what lets
        // you slide along a station's edge instead of stopping dead
        // the moment any part of a diagonal move touches it.
        position.y += moveY;
        bounds.y = position.y - SIZE / 2f;
        if (collidesAny(solidBounds)) {
            position.y -= moveY;
            bounds.y = position.y - SIZE / 2f;
        }

        // World-edge clamp (same as before) — the fallback "wall" until
        // real perimeter walls exist.
        position.x = MathUtils.clamp(position.x, 0f, RestaurantWorld.WIDTH);
        position.y = MathUtils.clamp(position.y, 0f, RestaurantWorld.HEIGHT);
        bounds.setPosition(position.x - SIZE / 2f, position.y - SIZE / 2f);
    }

    private boolean collidesAny(List<Rectangle> solidBounds) {
        for (Rectangle solid : solidBounds) {
            if (bounds.overlaps(solid)) {
                return true;
            }
        }
        return false;
    }

    private void updateFacing(Vector2 direction) {
        // Pick whichever axis has the larger movement component as the
        // "true" facing — matters mainly for diagonal input, where both
        // axes are nonzero at once.
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            facing = direction.x > 0 ? Facing.RIGHT : Facing.LEFT;
        } else {
            facing = direction.y > 0 ? Facing.UP : Facing.DOWN;
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Facing getFacing() {
        return facing;
    }
}