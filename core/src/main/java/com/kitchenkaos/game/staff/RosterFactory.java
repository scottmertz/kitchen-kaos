package com.kitchenkaos.game.staff;

import com.kitchenkaos.game.kitchen.StationType;

import java.util.List;

/**
 * Hardcoded placeholder roster. Real hiring (GDD §6) doesn't exist yet
 * — this exists purely so Step 8a's automation has someone to drive.
 */
public final class RosterFactory {

    private RosterFactory() {}

    public static List<NpcCook> createPlaceholderRoster() {
        NpcCook jamie = new NpcCook("Jamie")
                .withSkill(StationType.GRILL, 0.75f)
                .withSkill(StationType.FRYER, 0.7f)
                .withSkill(StationType.SAUTE, 0.3f)
                .assignTo(StationType.GRILL)
                .assignTo(StationType.FRYER);

        NpcCook alex = new NpcCook("Alex")
                .withSkill(StationType.SAUTE, 0.6f)
                .withSkill(StationType.COLD, 0.8f)
                .withSkill(StationType.PREP, 0.55f)
                .assignTo(StationType.SAUTE)
                .assignTo(StationType.COLD);

        return List.of(jamie, alex);
    }
}