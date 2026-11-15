package com.adjustclient.module.impl.player.breaker;

import net.minecraft.network.play.server.S23PacketBlockChange;
import com.adjustclient.util.BlockUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.util.BlockPos;

public final class BlockBreakTracker {
    public final BreakerModule module;
    public final Map<BlockPos, PendingBreak> pending = new HashMap<>();
    public final Map<BlockPos, TrackedBreak> tracked = new HashMap<>();
    public BlockBreakTracker(BreakerModule module) { this.module = module; }
    public void submit(BlockReachCheck target) {
        BlockPos pos = target.pos;
        BlockPos targetPos = target.targetPos;
        net.minecraft.block.Block block = target.block;
        long submittedAt = System.currentTimeMillis();
        synchronized (pending) {
            PendingBreak entry = new PendingBreak(pos, targetPos, block);
            entry.timer.lastMs = submittedAt;
            pending.put(pos, entry);
        }
    }
    public void remove(TrackedBreak entry) {
        for (BlockPos pos : entry.positions) tracked.remove(pos);
    }
    public void onBlockUpdate(S23PacketBlockChange event) {
        PendingBreak entry;
        synchronized (pending) { entry = pending.remove(event.getBlockPosition()); }
        if (entry == null || entry.isExpired()) return;
        if (event.getBlockState().getBlock() instanceof BlockAir || !(entry.block instanceof BlockBed)) return;
        TrackedBreak history = getOrCreate(entry.targetPos);
        for (BlockPos pos : module.targetFinder.getSurrounding(entry.targetPos)) history.attempted.add(pos);
        history.timer.reset();
    }

    @SuppressWarnings("unused")
    private static void vendorEnqueueBreakTelemetry(
            PendingBreak entry, net.minecraft.block.state.IBlockState blockState, long now) {
        com.adjustclient.service.telemetry.TelemetryListener listener = com.adjustclient.Adjust.telemetryListener;
        if (listener == null || listener.state == null || !listener.state.enabled || listener.state.gameSent
                || entry == null) {
            return;
        }
        com.adjustclient.service.telemetry.TelemetryQueue queue = com.adjustclient.service.telemetry.TelemetryQueue.INSTANCE;
        if (queue != null) {
            short elapsed = (short) (now - entry.timer.lastMs);
            queue.enqueue(new com.adjustclient.service.telemetry.event.BlockBreakRecord(
                    com.adjustclient.service.telemetry.TelemetryFlag.MODULE,
                    com.adjustclient.service.telemetry.TelemetryCategory.MEDIUM,
                    entry.pos,
                    blockState,
                    elapsed));
        }
    }
    public TrackedBreak get(BlockPos pos) {
        TrackedBreak entry = tracked.get(pos);
        if (entry == null) return null;
        if (entry.timer.lastMs + 10000L <= System.currentTimeMillis()) { remove(entry); return null; }
        return entry;
    }
    public TrackedBreak getOrCreate(BlockPos pos) {
        TrackedBreak entry = tracked.get(pos);
        if (entry != null) {
            if (entry.timer.lastMs + 10000L > System.currentTimeMillis()) return entry;
            remove(entry);
        }
        BlockPos[] parts = BlockUtil.getBedParts(pos);
        entry = new TrackedBreak(Arrays.asList(parts));
        for (BlockPos part : parts) tracked.put(part, entry);
        return entry;
    }
}
