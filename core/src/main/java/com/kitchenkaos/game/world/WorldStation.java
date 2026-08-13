package com.kitchenkaos.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.kitchenkaos.game.kitchen.Station;
import com.kitchenkaos.game.orders.Ticket;
import com.kitchenkaos.game.pos.PosMenu;
import com.kitchenkaos.game.pos.PosMenuItem;
import com.kitchenkaos.game.sim.FlowMeter;

import java.util.ArrayList;
import java.util.List;

/**
 * The world-space representation of a Station. Unlike WorldPOS's fixed
 * menu, this station's menu contents are rebuilt EVERY time it's opened
 * (see refreshMenu()) since which tickets are waiting on this station
 * changes shift to shift. Ingredient selection is stubbed for now — no
 * real inventory system exists yet (flagged separately); each menu
 * entry is just the dish name, selecting it starts the cook directly.
 */
public class WorldStation implements Interactable {

    private final Station station;
    private final Rectangle bounds;
    private final PosMenu menu = new PosMenu(new ArrayList<>());

    public WorldStation(Station station, float x, float y, float width, float height) {
        this.station = station;
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Station getStation() {
        return station;
    }

    public PosMenu getMenu() {
        return menu;
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public String getLabel() {
        return station.getType() + " Station";
    }

    @Override
    public void interact() {
        // No-op — actual menu population needs the active ticket list,
        // which lives in ShiftScreen. ShiftScreen calls refreshMenu()
        // itself right before opening this station's menu instead of
        // going through interact(), so this method intentionally does
        // nothing on its own.
    }

    /**
     * Rebuilds this station's menu from whichever tickets currently have
     * an uncooked dish routed here. Called by ShiftScreen right before
     * opening the menu, since Station/WorldStation don't have access to
     * the active ticket list themselves — that stays owned by ShiftScreen.
     */
    public void refreshMenu(List<Ticket> activeTickets, FlowMeter flow) {
        List<PosMenuItem> items = new ArrayList<>();

        if (station.isBusy()) {
            items.add(new PosMenuItem(
                    "Cooking in progress (" + (int) (station.getProgress() * 100f) + "%)",
                    false, null));
        } else {
            for (Ticket ticket : activeTickets) {
                for (int i = 0; i < ticket.getDishes().length; i++) {
                    if (ticket.isDishComplete(i)) {
                        continue;
                    }
                    if (ticket.getDishes()[i].getPrimaryStation() != station.getType()) {
                        continue;
                    }
                    // Capture as effectively-final locals for the lambda below.
                    Ticket capturedTicket = ticket;
                    int capturedIndex = i;
                    items.add(new PosMenuItem(
                            "Cook: " + ticket.getDishes()[i].getName(),
                            true,
                            () -> station.startTask(
                                    capturedTicket.getDishes()[capturedIndex].getBaseCookSeconds(), flow)
                    ));
                }
            }
            if (items.isEmpty()) {
                items.add(new PosMenuItem("(No tickets waiting)", false, null));
            }
        }

        items.add(new PosMenuItem("Exit", true, menu::close));
        menu.setItems(items);
    }
}