package com.adjustclient.modules.impl.player.breaker;

import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public final class PendingBreak {
    public final BlockPos pos;
    public final BlockPos targetPos;
    public final Block block;
    public final Timer timer = new Timer();
    public PendingBreak(BlockPos pos, BlockPos targetPos, Block block) {
        this.pos = pos; this.targetPos = targetPos; this.block = block;
    }
    public boolean isExpired() {
        var state = ServerTracker.INSTANCE.serverState;
        int delay = state == null ? 0 : state.packetWatcher.getAverageDelay();
        return timer.lastMs + delay + 500L <= System.currentTimeMillis();
    }
}
