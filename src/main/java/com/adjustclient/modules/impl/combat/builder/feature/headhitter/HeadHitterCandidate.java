package com.adjustclient.modules.impl.combat.builder.feature.headhitter;

import java.util.*;
import net.minecraft.util.BlockPos;
import com.adjustclient.modules.impl.combat.builder.BuildTarget;

public record HeadHitterCandidate(
        HeadHitterPlan plan,
        HeadHitterSupport support,
        double overlap) {
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HeadHitterCandidate value)) return false;
        return Objects.equals(plan, value.plan) && Objects.equals(support, value.support) && Double.compare(overlap, value.overlap) == 0;
    }
    @Override public int hashCode() {
        int hash = 0;
        hash = 31 * hash + Objects.hashCode(plan);
        hash = 31 * hash + Objects.hashCode(support);
        hash = 31 * hash + Double.hashCode(overlap);
        return hash;
    }
    @Override public String toString() {
        return "d[plan=" + plan + ", support=" + support + ", overlap=" + overlap + "]";
    }
}
