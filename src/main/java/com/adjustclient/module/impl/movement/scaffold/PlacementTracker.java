package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.module.impl.movement.StasisModule;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.PacketWatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class PlacementTracker implements MinecraftInstance {

    private final ScaffoldModule module;
    private final Map<BlockPos, List<Timer>> placementTimes = new HashMap<>();
    private final List<BlockPlacementRecord> entries = new ArrayList<>();
    private TimedValue pending;

    public PlacementTracker(ScaffoldModule module) {
        this.module = module;
    }

    public void clear() {
        this.placementTimes.clear();
        this.entries.clear();
        this.pending = null;
    }

    public boolean hasPending() {
        TimedValue pending = this.pending;
        if (pending == null) {
            return false;
        }
        if (pending.timer.hasElapsed(pending.duration)) {
            this.pending = null;
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        Vec3 playerPos = new Vec3(player.posX, player.posY, player.posZ);
        double dx = ((Vec3) pending.value).xCoord - playerPos.xCoord;
        double dy = ((Vec3) pending.value).yCoord - playerPos.yCoord;
        double dz = ((Vec3) pending.value).zCoord - playerPos.zCoord;
        if (MathHelper.sqrt_double(dx * dx + dy * dy + dz * dz) > 3.0F) {
            this.pending = null;
            return false;
        }
        return true;
    }

    public void trackPlacement(PlacementTarget placement) {
        if (mc.integratedServerIsRunning) {
            return;
        }
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        if (held == null) {
            return;
        }
        if (held.getItem() instanceof ItemBlock) {
            BlockPos target = placement.pos.offset(placement.facing);
            Iterator<BlockPlacementRecord> iterator = this.entries.iterator();
            while (iterator.hasNext()) {
                BlockPlacementRecord entry = iterator.next();
                if (entry.newBlockPos.equals(target)) {
                    if (!entry.confirmed) {
                        return;
                    }
                    this.entries.remove(entry);
                    break;
                }
            }
            IBlockState state = mc.theWorld.getBlockState(target);
            Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            int slot = mc.thePlayer.inventory.m();
            this.entries.add(new BlockPlacementRecord(placement.pos, placement.facing,
                    ((ItemBlock) held.getItem()).getBlock(), state, playerPos, slot));
        }
    }

    public void recordPlacementTime(BlockPos pos) {
        List<Timer> times = this.placementTimes.get(pos);
        if (times == null) {
            times = new ArrayList<>();
            this.placementTimes.put(pos, times);
        }
        times.add(new Timer());
    }

    public void removeExpired() {
        for (int i = 0; i < this.entries.size(); ) {
            BlockPlacementRecord entry = this.entries.get(i);
            long fade = 550L;
            if (ServerTracker.INSTANCE.serverState != null) {
                PacketWatcher accumulator = ServerTracker.INSTANCE.serverState.packetWatcher;
                if (accumulator.delayCount != 0) {
                    fade = accumulator.totalDelay / accumulator.delayCount + 550L;
                }
            }
            if (entry.placedTime.hasElapsed(fade)) {
                this.entries.remove(i);
            } else {
                i++;
            }
        }
    }

    public void onBlockChange(S23PacketBlockChange packet) {
        if (mc.thePlayer == null) {
            return;
        }
        BlockPlacementRecord entry = null;
        for (BlockPlacementRecord candidate : this.entries) {
            BlockPos changed = packet.getBlockPosition();
            if (!candidate.newBlockPos.equals(changed)) {
                continue;
            }
            int y = changed.getY();
            ScaffoldMode mode = this.module.settings.mode.getValue();
            if (!mode.isAboveBuildLimit(y)) {
                entry = candidate;
                break;
            }
        }
        if (entry == null) {
            return;
        }
        boolean wasConfirmed = entry.confirmed;
        entry.confirmed = true;
        entry.accepted = Blocks.air != packet.getBlockState().getBlock();
        entry.confirmedTime.reset();

        if (entry.accepted) {
            if (!wasConfirmed) {
                int counter = ServerTracker.INSTANCE.ticks;
                if (counter >= 1) {
                    ServerTracker.INSTANCE.ticks = counter - 1;
                }
            }
            return;
        }

        ServerTracker.INSTANCE.hotbarBlockTracker.forcedSlot = Integer.valueOf(entry.heldSlot);
        if (this.module.settings.mode.getValue() instanceof HypixelScaffoldMode) {
            ServerTracker.INSTANCE.ticks = 9;
            this.pending = new TimedValue(entry.playerPosition, 750);
        }
        if (this.module.settings.isAutoStasis()) {
            StasisModule.INSTANCE.setEnabled(true, true);
        }
    }
}
