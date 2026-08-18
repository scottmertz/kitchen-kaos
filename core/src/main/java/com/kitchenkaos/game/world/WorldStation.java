package com.kitchenkaos.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.kitchenkaos.game.kitchen.Station;
import com.kitchenkaos.game.orders.Ticket;
import com.kitchenkaos.game.pos.PosMenu;
import com.kitchenkaos.game.pos.PosMenuItem;
import com.kitchenkaos.game.sim.FlowMeter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * World-space wrapper around a cooking Station. Menu shows one status
 * line per slot (busy %/ready-awaiting-collection/idle), followed by
 * any cookable dishes IF at least one slot is free. Ingredient
 * selection still stubbed (flagged separately) — selecting an item
 * cooks it directly.
 *
 * onSlotEvent is how a player Collect action reaches ShiftScreen —
 * WorldStation has no access to problemLog/roster/mistake-rolling
 * itself, so it just reports the raw event upward.
 */
public class WorldStation implements Interactable {

    private final Station station;
    private final Rectangle bounds;
    private final PosMenu menu = new PosMenu(new ArrayList<>());
    private final Consumer<Station.SlotEvent> onSlotEvent;

    public WorldStation(Station station, float x, float y, float width, float height,
                        Consumer<Station.SlotEvent> onSlotEvent) {
        this.station = station;
        this.bounds = new Rectangle(x, y, width, height);
        this.onSlotEvent = onSlotEvent;
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
            if (station.isSlotReady(i)) {
                int slotIndex = i;
                items.add(new PosMenuItem(
                        "Slot " + (i + 1) + ": READY — " + station.getSlotDishName(i) + " (collect before it burns!)",
                        true,
                        () -> {
                            Station.SlotEvent event = station.collect(slotIndex);
                            if (event != null) {
                                onSlotEvent.accept(event);
                            }
                        }));
            } else {
                String label = station.isSlotBusy(i)
                        ? String.format("Slot %d: busy (%.0f%%)", i + 1, station.getSlotProgress(i) * 100f)
                        : "Slot " + (i + 1) + ": idle";
                items.add(new PosMenuItem(label, false, null));
            }
        }

        if (station.isCleaning()) {
            items.add(new PosMenuItem("Cleaning...", false, null));
        } else if (station.needsCleaning()) {
            items.add(new PosMenuItem(
                    "Station needs cleaning (" + station.getCompletionsSinceClean() + "/"
                            + station.getType().cleanThreshold + ")",
                    false, null));
            items.add(new PosMenuItem("Clean Station", true, station::startCleaning));
        } else {
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
                                        capturedTicket, capturedIndex,
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
        }

        items.add(new PosMenuItem("Exit", true, menu::close));
        menu.setItems(items);
    }
}