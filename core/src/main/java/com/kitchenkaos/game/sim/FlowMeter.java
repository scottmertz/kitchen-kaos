package com.kitchenkaos.game.sim;

import com.badlogic.gdx.math.MathUtils;
import com.kitchenkaos.game.GameConstants;

/**
 * Tracks the Flow Meter: good/bad timing raises or lowers it, and the
 * current value modifies station speed/accuracy (referenced throughout
 * the GDD). This class only knows about the NUMBER — it has no idea
 * what action triggered a change. Whatever calls onGoodTiming() etc.
 * is responsible for deciding when those calls happen.
 */
public class FlowMeter {

    private float flow = GameConstants.FLOW_START;

    public void onGoodTiming() {
        adjust(GameConstants.FLOW_GOOD_TIMING_GAIN);
    }

    public void onBadTiming() {
        adjust(-GameConstants.FLOW_BAD_TIMING_LOSS);
    }

    public void onMistake() {
        adjust(-GameConstants.FLOW_MISTAKE_LOSS);
    }

    private void adjust(float delta) {
        // MathUtils.clamp keeps flow inside [FLOW_MIN, FLOW_MAX] no matter
        // what — so callers never have to worry about it going negative
        // or over 100.
        flow = MathUtils.clamp(flow + delta, GameConstants.FLOW_MIN, GameConstants.FLOW_MAX);
    }

    public float getFlow() {
        return flow;
    }

    /**
     * Converts the current flow value into a speed/accuracy multiplier
     * for station logic to use later. 1.0 = normal, below 1.0 = at-risk
     * (slower, more mistake-prone), above 1.0 = "in the zone" bonus.
     */
    public float getSpeedMultiplier() {
        if (flow <= GameConstants.FLOW_RISK_THRESHOLD) {
            // Linearly worse the closer flow gets to zero.
            // lerp(0.6, 1.0, t) means: at flow=0 you're at 0.6x speed,
            // at flow=RISK_THRESHOLD you're back to exactly 1.0x.
            return MathUtils.lerp(0.6f, 1.0f, flow / GameConstants.FLOW_RISK_THRESHOLD);
        }
        if (flow >= GameConstants.FLOW_BONUS_THRESHOLD) {
            float t = (flow - GameConstants.FLOW_BONUS_THRESHOLD)
                    / (GameConstants.FLOW_MAX - GameConstants.FLOW_BONUS_THRESHOLD);
            return MathUtils.lerp(1.0f, 1.25f, t);
        }
        return 1.0f; // the "neutral middle" zone between risk and bonus thresholds
    }

    public boolean isAtRisk() {
        return flow <= GameConstants.FLOW_RISK_THRESHOLD;
    }

    public boolean isInTheZone() {
        return flow >= GameConstants.FLOW_BONUS_THRESHOLD;
    }
}