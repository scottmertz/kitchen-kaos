package com.kitchenkaos.game.pos;

import java.util.List;

/**
 * A navigable list of PosMenuItems with open/closed state. Pure logic —
 * no rendering or input polling happens here. ShiftScreen owns reading
 * actual keyboard/mouse input and calling moveSelection()/selectCurrent()
 * in response, same separation of concerns as Station/FlowMeter.
 */
public class PosMenu {

    private final List<PosMenuItem> items;
    private int selectedIndex = 0;
    private boolean open = false;

    public PosMenu(List<PosMenuItem> items) {
        this.items = items;
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