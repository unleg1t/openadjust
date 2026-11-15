package com.adjustclient.module.impl.combat.builder.feature.beddefense;

import com.adjustclient.module.impl.combat.builder.BuilderBlockFilter;
import java.util.List;

record BedDefensePlan(BedDefensePreset preset, List<BuilderBlockFilter> positions, boolean complete) {
    @Override public String toString() {
        return "d[preset=" + preset + ", positions=" + positions + ", complete=" + complete + "]";
    }
}
