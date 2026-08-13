package com.kitchenkaos.game.pos;

/**
 * One entry in a POS menu. enabled=false entries are visible but can't
 * be selected — used for menu categories that exist in the shell but
 * don't have real functionality wired up yet (Orders, Financials, etc).
 */
public class PosMenuItem {

    private final String label;
    private final boolean enabled;
    private final Runnable action;

    public PosMenuItem(String label, boolean enabled, Runnable action) {
        this.label = label;
        this.enabled = enabled;
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void select() {
        if (enabled && action != null) {
            action.run();
        }
    }
}