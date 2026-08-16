package com.kitchenkaos.game.pos;

import java.util.List;

/**
 * A navigable list of PosMenuItems with open/closed state. Pure logic —
 * no rendering or input polling happens here. ShiftScreen owns reading
 * actual keyboard/mouse input and calling moveSelection()/selectCurrent()
 * in response, same separation of concerns as Station/FlowMeter.
 */
public class PosMenu {

    private List<PosMenuItem> items;
    private int selectedIndex = 0;
    private boolean open = false;

    public PosMenu(List<PosMenuItem> items) {
        this.items = items;
    }

    /**
     * Replaces the item list. Preserves the current selectedIndex where
     * possible (so a live-refreshing menu, e.g. a station open every
     * frame, doesn't snap the cursor back to the top) — only resets if
     * the new list is empty, or clamps if it's shorter than before.
     */
    public void setItems(List<PosMenuItem> items) {
        this.items = items;
        if (items.isEmpty()) {
            selectedIndex = 0;
        } else if (selectedIndex >= items.size()) {
            selectedIndex = items.size() - 1;
        }
        // else: leave selectedIndex where it was
    }

    public void open() {
        open = true;
        selectedIndex = 0;
    }

    public void close() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void moveSelection(int direction) {
        int count = items.size();
        // +count before % handles negative wraparound cleanly (moving up
        // from index 0 should land on the last item, not go negative).
        selectedIndex = (selectedIndex + direction + count) % count;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < items.size()) {
            selectedIndex = index;
        }
    }

    public void selectCurrent() {
        items.get(selectedIndex).select();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<PosMenuItem> getItems() {
        return items;
    }
}