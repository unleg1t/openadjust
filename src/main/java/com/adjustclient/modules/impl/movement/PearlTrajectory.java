package com.adjustclient.modules.impl.movement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.BlockPos;

public class PearlTrajectory {
    public List<BlockPos> positions;
    public List<?> results;

    public PearlTrajectory(List<BlockPos> positions) {
        this.positions = positions;
        this.results = new ArrayList<>();
    }
}
