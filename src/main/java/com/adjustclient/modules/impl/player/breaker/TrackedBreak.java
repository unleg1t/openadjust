package com.adjustclient.modules.impl.player.breaker;

import com.adjustclient.utilities.Timer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.BlockPos;

public final class TrackedBreak {
    public final Timer timer = new Timer();
    public final List<BlockPos> positions;
    public final Set<BlockPos> attempted = new HashSet<>(1);
    public TrackedBreak(List<BlockPos> positions) { this.positions = positions; }
}
