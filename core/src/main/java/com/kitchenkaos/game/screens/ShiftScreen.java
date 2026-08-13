package com.kitchenkaos.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kitchenkaos.game.kitchen.Station;
import com.kitchenkaos.game.kitchen.StationType;
import com.kitchenkaos.game.menu.Dish;
import com.kitchenkaos.game.orders.Ticket;
import com.kitchenkaos.game.orders.TicketSpawner;
import com.kitchenkaos.game.problems.ProblemEvent;
import com.kitchenkaos.game.problems.ProblemType;
import com.kitchenkaos.game.sim.FlowMeter;
import com.kitchenkaos.game.sim.TimeCompressionClock;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.math.Vector2;
import com.kitchenkaos.game.GameConstants;

/**
 * Orchestrates one shift: owns the clock, flow meter, stations, ticket
 * spawner, and problem log, and wires them together each frame.
 *
 * IMPORTANT — current limitation: there's no player input system yet,
 * so ticket->station assignment and completion below is fully automatic
 * ("autopilot"), just to prove the loop end-to-end. Real player-driven
 * interaction (click a station, pull food off in time, etc.) replaces
 * the autopilot logic in a later step — search "TODO(player-input)"
 * below for exactly where that hook goes.
 */
public class ShiftScreen implements Screen {

    private final TimeCompressionClock clock = new TimeCompressionClock();
    private final FlowMeter flow = new FlowMeter();
    private final TicketSpawner ticketSpawner = new TicketSpawner();

    private final Map<StationType, Station> stations = new EnumMap<>(StationType.class);
    private final List<Ticket> activeTickets = new ArrayList<>();
    private final List<ProblemEvent> problemLog = new ArrayList<>();

    private com.badlogic.gdx.graphics.OrthographicCamera camera;
    private com.kitchenkaos.game.world.Player player;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;
    private final java.util.List<com.kitchenkaos.game.world.WorldStation> worldStations = new ArrayList<>();
    private final java.util.List<com.badlogic.gdx.math.Rectangle> solidBounds = new ArrayList<>();

    private final com.kitchenkaos.game.sim.ShiftStateMachine shiftState = new com.kitchenkaos.game.sim.ShiftStateMachine();

    // Tracks which ticket/dish each BUSY station is currently working on,
    // so that when Station.update() reports "done", we know which Ticket
    // to mark complete. Keyed by StationType since each station can only
    // work one thing at a time.
    private final Map<StationType, Assignment> currentAssignments = new EnumMap<>(StationType.class);

    // Tickets we've already fired a LONG_WAIT problem for, so we don't
    // spam a new ProblemEvent every single frame a ticket stays overdue.
    private final List<Ticket> alreadyFlaggedForLongWait = new ArrayList<>();

    private static final float LONG_WAIT_SECONDS = 240f; // 4 real minutes sitting unfulfilled

    private SpriteBatch batch;
    private BitmapFont font;

    /** Pairs a Ticket with which dish-index on it is currently being cooked. */
    private static class Assignment {
        final Ticket ticket;
        final int dishIndex;
        Assignment(Ticket ticket, int dishIndex) {
            this.ticket = ticket;
            this.dishIndex = dishIndex;
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // libGDX's built-in default font — placeholder, fine for now

        float x = 150f;
        float y = 400f;
        float stationSize = 96f;
        float spacing = 160f;

        for (StationType type : StationType.values()) {
            Station station = new Station(type);
            stations.put(type, station);

            com.kitchenkaos.game.world.WorldStation worldStation =
                    new com.kitchenkaos.game.world.WorldStation(station, x, y, stationSize, stationSize);
            worldStations.add(worldStation);
            solidBounds.add(worldStation.getBounds());

            x += spacing;
        }

        camera = new com.badlogic.gdx.graphics.OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        player = new com.kitchenkaos.game.world.Player(
                com.kitchenkaos.game.world.RestaurantWorld.WIDTH / 2f,
                200f // clear of the station row (which sits at y=400+), so player doesn't spawn overlapping
        );

        shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        // TEMPORARY: real clock-in is the POS (Step 4). Until that exists,
        // press C to clock in and X to clock out, just to unblock testing
        // everything downstream of ShiftState.
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
            shiftState.clockIn();
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.X)) {
            shiftState.clockOut();
        }

        if (shiftState.isClockedIn()) {
            clock.update(delta);
            shiftState.update(clock.getPhase());
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            com.kitchenkaos.game.world.WorldStation target = findInteractableInRange();
            if (target != null) {
                target.interact();
            }
        }

        player.update(delta, solidBounds);
        updateCamera();

        // 1. Spawn new tickets, queue their dishes for assignment.
        if (shiftState.isOpenForCustomers()) {
            Ticket newTicket = ticketSpawner.update(delta, clock.getHoursSinceStart(), clock.getPhase());
            if (newTicket != null) {
                activeTickets.add(newTicket);
            }
        }

        // 2. Free stations pick up waiting dishes (autopilot — see class comment).
        assignWaitingDishesToFreeStations();

        // 3. Advance every busy station; handle completions.
        for (Station station : stations.values()) {
            boolean justFinished = station.update(delta);
            if (justFinished) {
                onStationFinished(station);
            }
        }

        // 4. Long wait detection — real logic, not a stub.
        for (Ticket ticket : activeTickets) {
            if (alreadyFlaggedForLongWait.contains(ticket)) {
                continue;
            }
            float waitSeconds = ticket.getWaitSeconds(clock.getHoursSinceStart(), 60f);
            if (waitSeconds >= LONG_WAIT_SECONDS) {
                problemLog.add(new ProblemEvent(
                        ProblemType.LONG_WAIT,
                        clock.getHoursSinceStart(),
                        "Table waited " + (int) (waitSeconds / 60f) + " min"
                ));
                alreadyFlaggedForLongWait.add(ticket);
            }
        }

        // TODO(player-input): BURNED_FOOD should fire here when a station
        // finishes a task but the PLAYER fails to collect/pull it within
        // some grace window — not automatically, like everything above.
        // Needs: a "ready but uncollected" state on Station, plus an
        // actual input system to let the player collect it. Neither
        // exists yet.

        // TODO(player-input / vendor system): EIGHTY_SIXED_INGREDIENT
        // should fire from a vendor/inventory system deciding an
        // ingredient ran out — that system doesn't exist yet either.

        // 5. Remove fulfilled tickets from the active list.
        activeTickets.removeIf(Ticket::isFulfilled);
    }

    /**
     * Returns the nearest WorldStation the player is both within range of
     * AND facing (using a dot-product cone, not pixel-perfect aim — makes
     * interaction feel forgiving rather than finicky). Returns null if
     * nothing qualifies.
     */
    private com.kitchenkaos.game.world.WorldStation findInteractableInRange() {
        Vector2 facingVec = new Vector2(player.getFacing().dx, player.getFacing().dy);

        for (com.kitchenkaos.game.world.WorldStation ws : worldStations) {
            com.badlogic.gdx.math.Rectangle b = ws.getBounds();
            Vector2 center = new Vector2(b.x + b.width / 2f, b.y + b.height / 2f);

            Vector2 toTarget = center.cpy().sub(player.getPosition());
            float distance = toTarget.len();
            if (distance > GameConstants.INTERACTION_RANGE) {
                continue;
            }

            toTarget.nor();
            float dot = toTarget.dot(facingVec);
            if (dot >= GameConstants.FACING_DOT_THRESHOLD) {
                return ws;
            }
        }
        return null;
    }

    private void updateCamera() {
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        // Follow the player...
        float camX = player.getPosition().x;
        float camY = player.getPosition().y;

        // ...but clamp so the camera never shows past the world's edges.
        camX = com.badlogic.gdx.math.MathUtils.clamp(
                camX, halfWidth, com.kitchenkaos.game.world.RestaurantWorld.WIDTH - halfWidth);
        camY = com.badlogic.gdx.math.MathUtils.clamp(
                camY, halfHeight, com.kitchenkaos.game.world.RestaurantWorld.HEIGHT - halfHeight);

        camera.position.set(camX, camY, 0);
        camera.update();
    }
    private void assignWaitingDishesToFreeStations() {
        for (Ticket ticket : activeTickets) {
            Dish[] dishes = ticket.getDishes();
            for (int i = 0; i < dishes.length; i++) {
                Dish dish = dishes[i];
                StationType neededType = dish.getPrimaryStation();
                Station station = stations.get(neededType);

                boolean alreadyAssigned = isAlreadyAssigned(ticket, i);
                boolean alreadyDone = ticket.isDishComplete(i);
                if (!station.isBusy() && !alreadyAssigned && !alreadyDone) {
                    station.startTask(dish.getBaseCookSeconds(), flow);
                    currentAssignments.put(neededType, new Assignment(ticket, i));
                    // Autopilot always counts as "good timing" for now —
                    // this is exactly the kind of call that becomes real
                    // player skill once input exists.
                    flow.onGoodTiming();
                }
            }
        }
    }

    private boolean isAlreadyAssigned(Ticket ticket, int dishIndex) {
        for (Assignment assignment : currentAssignments.values()) {
            if (assignment.ticket == ticket && assignment.dishIndex == dishIndex) {
                return true;
            }
        }
        return false;
    }

    private void onStationFinished(Station station) {
        Assignment assignment = currentAssignments.remove(station.getType());
        if (assignment != null) {
            assignment.ticket.markDishComplete(assignment.dishIndex);
        }
    }

    private void draw() {
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.12f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.3f, 0.5f, 0.8f, 1f); // blue = stations
        for (com.kitchenkaos.game.world.WorldStation ws : worldStations) {
            com.badlogic.gdx.math.Rectangle b = ws.getBounds();
            shapeRenderer.rect(b.x, b.y, b.width, b.height);
        }

        shapeRenderer.setColor(1f, 0.6f, 0.2f, 1f); // orange = player
        shapeRenderer.rect(player.getPosition().x - 16, player.getPosition().y - 16, 32, 32);

        shapeRenderer.end();

        batch.begin();
        int y = 700;
        font.draw(batch, "Shift state: " + shiftState.getState(), 20, y);
        y -= 25;
        font.draw(batch, String.format("Time: %02d:%02d (%s)",
                clock.getDisplayHour24(), clock.getDisplayMinute(), clock.getPhase().label), 20, y);
        y -= 25;
        font.draw(batch, String.format("Flow: %.0f", flow.getFlow()), 20, y);
        y -= 25;
        font.draw(batch, "Active tickets: " + activeTickets.size(), 20, y);
        y -= 25;
        font.draw(batch, "Problems logged: " + problemLog.size(), 20, y);
        y -= 35;
        com.kitchenkaos.game.world.WorldStation nearby = findInteractableInRange();
        if (nearby != null) {
            font.draw(batch, "Press SPACE to interact: " + nearby.getLabel(), 20, y);
            y -= 25;
        }

        for (StationType type : StationType.values()) {
            Station station = stations.get(type);
            String status = station.isBusy()
                    ? String.format("%s: busy (%.0f%%)", type, station.getProgress() * 100f)
                    : type + ": idle";
            font.draw(batch, status, 20, y);
            y -= 20;
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}