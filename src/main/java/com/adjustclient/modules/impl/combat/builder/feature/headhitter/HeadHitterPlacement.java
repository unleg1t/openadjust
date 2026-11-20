package com.adjustclient.modules.impl.combat.builder.feature.headhitter;

import java.util.*;
import net.minecraft.util.BlockPos;
import com.adjustclient.modules.impl.combat.builder.BuildTarget;

public record HeadHitterPlacement(
        HeadHitterSupport support,
        double rotationDifference,
        BuildTarget target) {
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HeadHitterPlacement value)) return false;
        return Double.compare(rotationDifference, value.rotationDifference) == 0 && Objects.equals(target, value.target) && Objects.equals(support, value.support);
    }
    @Override public int hashCode() {
        int hash = 0;
        hash = 31 * hash + Objects.hashCode(support);
        hash = 31 * hash + Objects.hashCode(target);
        hash = 31 * hash + Double.hashCode(rotationDifference);
        return hash;
    }
    @Override public String toString() {
        String supportText = String.valueOf(support);
        String targetText = target == null ? "null" : "com.adjustclient.a.b.b.e.a.e.b@" + Integer.toHexString(System.identityHashCode(target));
        return "c[support=" + supportText + ", target=" + targetText + ", rotationDifference=" + rotationDifference + "]";
    }
}
