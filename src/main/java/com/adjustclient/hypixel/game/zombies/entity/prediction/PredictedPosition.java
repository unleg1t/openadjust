package com.adjustclient.hypixel.game.zombies.entity.prediction;

import java.util.Objects;
import net.minecraft.util.Vec3;

public record PredictedPosition(Vec3 position, float accuracyEstimate) {
    @Override public boolean equals(Object other) {
        return this == other || other instanceof PredictedPosition p
            && Float.compare(accuracyEstimate, p.accuracyEstimate) == 0
            && Objects.equals(position, p.position);
    }
    @Override public int hashCode() { return 31 * Objects.hashCode(position) + Float.hashCode(accuracyEstimate); }
    @Override public String toString() { return "a[position=" + position + ", accuracyEstimate=" + accuracyEstimate + "]"; }
}
