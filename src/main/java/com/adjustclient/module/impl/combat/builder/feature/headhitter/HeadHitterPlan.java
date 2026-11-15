package com.adjustclient.module.impl.combat.builder.feature.headhitter;

import java.util.*;
import net.minecraft.util.BlockPos;
import com.adjustclient.module.impl.combat.builder.BuildTarget;

public record HeadHitterPlan(
        BlockPos landing,
        BlockPos roof,
        List<net.minecraft.util.AxisAlignedBB> movement) {
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HeadHitterPlan value)) return false;
        return Objects.equals(movement, value.movement) && Objects.equals(roof, value.roof) && Objects.equals(landing, value.landing);
    }
    @Override public int hashCode() {
        int hash = 0;
        hash = 31 * hash + Objects.hashCode(landing);
        hash = 31 * hash + Objects.hashCode(roof);
        hash = 31 * hash + Objects.hashCode(movement);
        return hash;
    }
    @Override public String toString() {
        return "b[landing=" + landing + ", roof=" + roof + ", movement=" + movement + "]";
    }
}
