package com.kitchenkaos.game.world;

import com.badlogic.gdx.math.Rectangle;
import com.kitchenkaos.game.orders.Ticket;
import com.kitchenkaos.game.pos.PosMenu;
import com.kitchenkaos.game.pos.PosMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Expo — not a cooking station. It's the final checkpoint: a ticket
 * only leaves activeTickets by being SENT here, and only once every
 * dish on it is complete. A fully-cooked ticket sitting unsent still
 * counts for LONG_WAIT — a plate dying under the lamp because nobody
 * walked it over is a real failure, not something to suppress.
 *
 * "Correctness" here currently only means "every dish marked complete."
 * There's no wrong-dish/substitution tracking yet — that's the GDD's
 * separate "wrong order" Tier 1 problem, not built (flagged, not stubbed
 * further than this comment).
 */
public class WorldExpo implements Interactable {

    private final Rectangle bounds;
    private final PosMenu menu = new PosMenu(new ArrayList<>());

    public WorldExpo(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
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
        return "Expo";
    }

    @Override
    public void interact() {
        // See refreshMenu() — same pattern as WorldStation.
    }

    public void refreshMenu(List<Ticket> activeTickets) {
        List<PosMenuItem> items = new ArrayList<>();

        for (Ticket ticket : activeTickets) {
            if (ticket.isSubmitted()) {
                continue;
            }
            String summary = ticketSummary(ticket);
            if (ticket.isFulfilled()) {
                items.add(new PosMenuItem("Send: " + summary, true, ticket::markSubmitted));
            } else {
                items.add(new PosMenuItem("Not ready: " + summary, false, null));
            }
        }

        if (items.isEmpty()) {
            items.add(new PosMenuItem("(No tickets to send)", false, null));
        }

        items.add(new PosMenuItem("Exit", true, menu::close));
        menu.setItems(items);
    }

    private String ticketSummary(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ticket.getDishes().length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ticket.getDishes()[i].getName());
        }
        return sb.toString();
    }
}