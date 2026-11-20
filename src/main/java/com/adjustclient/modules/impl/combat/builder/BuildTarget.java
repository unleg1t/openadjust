package com.adjustclient.modules.impl.combat.builder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumFacing;

public final class BuildTarget {
    public final List<BuilderBlockFilter> positions;
    public final ArrayList<BuilderBlockFilter> pending;
    public final int waitToPlace;
    public final boolean ordered;
    public final boolean rotate;
    public final boolean render;
    public boolean allDown;

    public BuildTarget(List<BuilderBlockFilter> positions, List<BuilderBlockFilter> pending,
            int waitToPlace, boolean ordered, boolean rotate, boolean render, boolean allDown) {
        this.positions = positions;
        this.pending = pending instanceof ArrayList ? (ArrayList<BuilderBlockFilter>) pending : new ArrayList<>(pending);
        this.waitToPlace = waitToPlace;
        this.ordered = ordered;
        this.rotate = rotate;
        this.render = render;
        this.allDown = allDown;
    }

    public void updateAllDown() {
        allDown = true;
        for (BuilderBlockFilter position : pending) {
            if (position.facing != EnumFacing.UP) {
                allDown = false;
                return;
            }
        }
    }
}
