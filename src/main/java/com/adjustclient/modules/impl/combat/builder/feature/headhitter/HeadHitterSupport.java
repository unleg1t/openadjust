package com.adjustclient.modules.impl.combat.builder.feature.headhitter;

import java.util.*;
import net.minecraft.util.BlockPos;
import com.adjustclient.modules.impl.combat.builder.BuildTarget;

public record HeadHitterSupport(
        List<BlockPos> positions,
        double clearance) {
    public BlockPos lastPosition() { return positions.getLast(); }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HeadHitterSupport value)) return false;
        return Double.compare(clearance, value.clearance) == 0 && Objects.equals(positions, value.positions);
    }
    @Override public int hashCode() {
        int hash = 0;
        hash = 31 * hash + Objects.hashCode(positions);
        hash = 31 * hash + Double.hashCode(clearance);
        return hash;
    }
    @Override public String toString() {
        return "e[positions=" + positions + ", clearance=" + clearance + "]";
    }
}
