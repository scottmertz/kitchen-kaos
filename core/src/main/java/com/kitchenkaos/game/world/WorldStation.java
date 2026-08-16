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
 * World-space wrapper around a cooking Station. Menu shows one
 * disabled status line per slot (busy %/idle), followed by any
 * cookable dishes IF at least one slot is free. Ingredient selection
 * still stubbed (flagged separately) — selecting an item cooks it
 * directly.
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
        // No-op — see refreshMenu(). ShiftScreen calls it directly
        // before opening this menu, since it needs the active ticket list.
    }

    public void refreshMenu(List<Ticket> activeTickets, FlowMeter flow) {
        List<PosMenuItem> items = new ArrayList<>();

        for (int i = 0; i < station.getSlotCount(); i++) {
            String label = station.isSlotBusy(i)
                    ? String.format("Slot %d: busy (%.0f%%)", i + 1, station.getSlotProgress(i) * 100f)
                    : "Slot " + (i + 1) + ": idle";
            items.add(new PosMenuItem(label, false, null));
        }

        boolean anyCookable = false;
        if (station.hasFreeSlot()) {
            for (Ticket ticket : activeTickets) {
                for (int i = 0; i < ticket.getDishes().length; i++) {
                    if (ticket.isDishComplete(i)) {
                        continue;
                    }
                    if (ticket.getDishes()[i].getPrimaryStation() != station.getType()) {
                        continue;
                    }
                    Ticket capturedTicket = ticket;
                    int capturedIndex = i;
                    items.add(new PosMenuItem(
                            "Cook: " + ticket.getDishes()[i].getName(),
                            true,
                            () -> station.startTask(
                                    capturedTicket.getDishes()[capturedIndex].getBaseCookSeconds(), flow)
                    ));
                    anyCookable = true;
                }
            }
        }

        if (!anyCookable) {
            items.add(new PosMenuItem(
                    station.hasFreeSlot() ? "(No tickets waiting)" : "(All slots busy)",
                    false, null));
        }

        items.add(new PosMenuItem("Exit", true, menu::close));
        menu.setItems(items);
    }
}