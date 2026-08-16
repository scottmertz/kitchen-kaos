package com.kitchenkaos.game.staff;

import com.kitchenkaos.game.kitchen.StationType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * A hardcoded placeholder NPC cook — real hiring/roster management is
 * a later system (GDD §6). Skill is 0.0-1.0 per station: higher skill
 * = faster completion (via getSpeedMultiplier, same shape as FlowMeter's)
 * and lower mistake chance. A station with no entry in the skill map
 * defaults to 0.5 (average) via getSkill().
 */
public class NpcCook {

    private final String name;
    private final Map<StationType, Float> skillByStation = new EnumMap<>(StationType.class);
    private final Set<StationType> assignedStations = EnumSet.noneOf(StationType.class);

    public NpcCook(String name) {
        this.name = name;
    }

    public NpcCook withSkill(StationType type, float skill) {
        skillByStation.put(type, skill);
        return this;
    }

    public NpcCook assignTo(StationType type) {
        assignedStations.add(type);
        return this;
    }

    public float getSkill(StationType type) {
        return skillByStation.getOrDefault(type, 0.5f);
    }

    /** Same shape as FlowMeter.getSpeedMultiplier(): skill 0 -> 0.6x speed, skill 1 -> 1.25x speed. */
    public float getSpeedMultiplier(StationType type) {
        float skill = getSkill(type);
        return com.badlogic.gdx.math.MathUtils.lerp(0.6f, 1.25f, skill);
    }

    /** Chance [0-1] this NPC botches a task at this station. Skilled cooks rarely mess up; weak ones often do. */
    public float getMistakeChance(StationType type) {
        float skill = getSkill(type);
        return com.badlogic.gdx.math.MathUtils.lerp(0.35f, 0.02f, skill);
    }

    public String getName() {
        return name;
    }

    public Set<StationType> getAssignedStations() {
        return assignedStations;
    }
}