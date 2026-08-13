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
    private com.badlogic.gdx.graphics.OrthographicCamera uiCamera;
    private com.kitchenkaos.game.world.Player player;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;
    private final java.util.List<com.kitchenkaos.game.world.WorldStation> worldStations = new ArrayList<>();
    private final java.util.List<com.kitchenkaos.game.world.Interactable> interactables = new ArrayList<>();
    private com.kitchenkaos.game.world.WorldPOS pos;
    private final java.util.List<com.badlogic.gdx.math.Rectangle> solidBounds = new ArrayList<>();
    private com.kitchenkaos.game.world.WorldStation openStationMenu = null;

    private String toastMessage = null;
    private float toastTimer = 0f;
    private static final float TOAST_DURATION_SECONDS = 2.5f;

    private void showToast(String message) {
        toastMessage = message;
        toastTimer = TOAST_DURATION_SECONDS;
    }

    private final com.kitchenkaos.game.sim.ShiftStateMachine shiftState = new com.kitchenkaos.game.sim.ShiftStateMachine();

    private void handleGenericMenuInput(com.kitchenkaos.game.pos.PosMenu menu) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            menu.moveSelection(-1);
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            menu.moveSelection(1);
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            menu.selectCurrent();
        }

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        java.util.List<com.badlogic.gdx.math.Rectangle> itemBounds = computePosMenuItemBounds(menu);
        for (int i = 0; i < itemBounds.size(); i++) {
            if (itemBounds.get(i).contains(mouseX, mouseY)) {
                menu.setSelectedIndex(i);
                if (Gdx.input.justTouched()) {
                    menu.selectCurrent();
                }
                break;
            }
        }
    }

    /**
     * Computes each menu item's on-screen rectangle, used for BOTH drawing
     * the menu and mouse hit-testing — keeping one shared layout formula
     * means the visible menu and the clickable area can never drift apart.
     */
    private java.util.List<com.badlogic.gdx.math.Rectangle> computePosMenuItemBounds(com.kitchenkaos.game.pos.PosMenu menu) {
        java.util.List<com.badlogic.gdx.math.Rectangle> bounds = new ArrayList<>();
        float startX = 440f;
        float startY = 500f;
        float itemHeight = 32f;

        for (int i = 0; i < menu.getItems().size(); i++) {
            bounds.add(new com.badlogic.gdx.math.Rectangle(startX, startY - (i * itemHeight), 320f, itemHeight - 4f));
        }
        return bounds;
    }

    // Tickets we've already fired a LONG_WAIT problem for, so we don't
    // spam a new ProblemEvent every single frame a ticket stays overdue.
    private final List<Ticket> alreadyFlaggedForLongWait = new ArrayList<>();

    private static final float LONG_WAIT_SECONDS = 240f; // 4 real minutes sitting unfulfilled

    private SpriteBatch batch;
    private BitmapFont font;

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

        interactables.addAll(worldStations);

        java.util.List<com.kitchenkaos.game.pos.PosMenuItem> posItems = new ArrayList<>();
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Clock In", true, () -> {
            shiftState.clockIn();
            showToast("Clocked In");
        }));
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Clock Out", true, () -> {
            shiftState.clockOut();
            showToast("Clocked Out");
        }));
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Orders (coming soon)", false, null));
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Financials (coming soon)", false, null));
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Save (coming soon)", false, null));
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Quit (coming soon)", false, null));

        com.kitchenkaos.game.pos.PosMenu posMenu = new com.kitchenkaos.game.pos.PosMenu(posItems);
        posItems.add(new com.kitchenkaos.game.pos.PosMenuItem("Exit", true, posMenu::close));

        pos = new com.kitchenkaos.game.world.WorldPOS(posMenu, 900f, 500f, 90f, 90f);
        interactables.add(pos);
        solidBounds.add(pos.getBounds());

        camera = new com.badlogic.gdx.graphics.OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        uiCamera = new com.badlogic.gdx.graphics.OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
        if (pos.getMenu().isOpen()) {
            handleGenericMenuInput(pos.getMenu());
        } else if (openStationMenu != null) {
            handleGenericMenuInput(openStationMenu.getMenu());
            if (!openStationMenu.getMenu().isOpen()) {
                openStationMenu = null; // menu closed itself (Exit was selected)
            }
        } else {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                com.kitchenkaos.game.world.Interactable target = findInteractableInRange();
                if (target instanceof com.kitchenkaos.game.world.WorldStation ws) {
                    ws.refreshMenu(activeTickets, flow);
                    ws.getMenu().open();
                    openStationMenu = ws;
                } else if (target != null) {
                    target.interact();
                }
            }
            player.update(delta, solidBounds);
        }

        updateCamera();

        if (shiftState.isClockedIn()) {
            clock.update(delta);
            shiftState.update(clock.getPhase());
        }

        // 1. Spawn new tickets, queue their dishes for assignment.
        if (shiftState.isOpenForCustomers()) {
            Ticket newTicket = ticketSpawner.update(delta, clock.getHoursSinceStart(), clock.getPhase());
            if (newTicket != null) {
                activeTickets.add(newTicket);
            }
        }

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

        if (toastTimer > 0f) {
            toastTimer -= delta;
            if (toastTimer <= 0f) {
                toastMessage = null;
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
    private com.kitchenkaos.game.world.Interactable findInteractableInRange() {
        Vector2 facingVec = new Vector2(player.getFacing().dx, player.getFacing().dy);

        for (com.kitchenkaos.game.world.Interactable obj : interactables) {
            com.badlogic.gdx.math.Rectangle b = obj.getBounds();
            Vector2 center = new Vector2(b.x + b.width / 2f, b.y + b.height / 2f);

            Vector2 toTarget = center.cpy().sub(player.getPosition());
            float distance = toTarget.len();
            if (distance > GameConstants.INTERACTION_RANGE) {
                continue;
            }

            toTarget.nor();
            float dot = toTarget.dot(facingVec);
            if (dot >= GameConstants.FACING_DOT_THRESHOLD) {
                return obj;
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

    private void onStationFinished(Station station) {
        for (Ticket ticket : activeTickets) {
            var dishes = ticket.getDishes();
            for (int i = 0; i < dishes.length; i++) {
                if (!ticket.isDishComplete(i) && dishes[i].getPrimaryStation() == station.getType()) {
                    ticket.markDishComplete(i);
                    return; // only one dish could have JUST finished at this station
                }
            }
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

        shapeRenderer.setColor(0.7f, 0.7f, 0.2f, 1f); // yellow = POS terminal
        com.badlogic.gdx.math.Rectangle posBounds = pos.getBounds();
        shapeRenderer.rect(posBounds.x, posBounds.y, posBounds.width, posBounds.height);

        shapeRenderer.end();

        batch.setProjectionMatrix(uiCamera.combined);

        batch.begin();
        int y = 700;
        if (toastMessage != null) {
            font.draw(batch, ">>> " + toastMessage + " <<<", 520, 690);
        }
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
        if (!pos.getMenu().isOpen() && openStationMenu == null) {
            com.kitchenkaos.game.world.Interactable nearby = findInteractableInRange();
            if (nearby != null) {
                font.draw(batch, "Press SPACE to interact: " + nearby.getLabel(), 20, y);
                y -= 25;
            }
        }

        com.kitchenkaos.game.pos.PosMenu activeMenu = null;
        if (pos.getMenu().isOpen()) {
            activeMenu = pos.getMenu();
        } else if (openStationMenu != null) {
            activeMenu = openStationMenu.getMenu();
        }
        if (activeMenu != null) {
            java.util.List<com.badlogic.gdx.math.Rectangle> itemBounds = computePosMenuItemBounds(activeMenu);
            for (int i = 0; i < activeMenu.getItems().size(); i++) {
                com.kitchenkaos.game.pos.PosMenuItem item = activeMenu.getItems().get(i);
                com.badlogic.gdx.math.Rectangle b = itemBounds.get(i);

                String prefix = (i == activeMenu.getSelectedIndex()) ? "> " : "  ";
                String suffix = item.isEnabled() ? "" : " (disabled)";
                font.draw(batch, prefix + item.getLabel() + suffix, b.x, b.y + b.height - 6);
            }
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