package com.adjustclient.modules.impl.player.fastuse.feature;

import java.util.ArrayList;
import net.minecraft.util.BlockPos;

public final class BreakProgressTracker {
    public final BlockBreakingFeature owner;
    public final ArrayList<BlockPos> positions;
    public BreakProgressTracker(BlockBreakingFeature owner) {
        this.owner = owner;
        this.positions = new ArrayList<>();
    }

    public void add(BlockPos pos) {
        positions.add(pos);
    }
}
