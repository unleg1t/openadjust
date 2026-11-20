package com.adjustclient.utilities.anticheat.blocker;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.MinecraftInstance;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.DataWatcher;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
public final class PacketWatcher implements MinecraftInstance {
    public BlockPos pendingBlock;
    public Timer placementTimer = new Timer();
    public Timer tabTimer = new Timer();
    public BlockedFeature pendingAction;
    public Timer actionTimer = new Timer();
    public Timer receiveTimer = new Timer();
    public IntArrayList samples = new IntArrayList(128);
    public int totalDelay;
    public int delayCount;
    public int field40;
    public int chunks;
    public int clockTicks;
    public boolean waitingTab;
    public Object field56;
    public static PacketWatcherThread thread;
    public int getAverageDelay() { return delayCount == 0 ? 0 : totalDelay / delayCount; }
    public void onPacket(Packet<?> packet, boolean outgoing, boolean applied) {
        if (outgoing) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement placement = (C08PacketPlayerBlockPlacement) packet;
                if (placement.getPlacedBlockDirection() != 255) {
                    if (pendingBlock != null) return;
                    if (placementTimer.hasElapsed(3000)) pendingBlock = placement.getPosition();
                    placementTimer.reset();
                } else {
                    if (pendingAction != null || mc.thePlayer == null) return;
                    ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
                    if (stack == null) return;
                    Item item = stack.getItem();
                    if (!(item instanceof ItemSword) && !(item instanceof ItemFood)
                            && (!(item instanceof ItemPotion) || (stack.getMetadata() & 0x4000) != 0)) return;
                    if (actionTimer.hasElapsed(3000)) pendingAction = new BlockedFeature(WatchedPacketType.START_USING_ITEM, this);
                    actionTimer.reset();
                }
            } else if (packet instanceof C14PacketTabComplete) {
                if (waitingTab) return;
                if (tabTimer.hasElapsed(3000)) waitingTab = true;
                tabTimer.reset();
            } else if (packet instanceof C07PacketPlayerDigging) {
                if (((C07PacketPlayerDigging) packet).getStatus() != C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
                        || pendingAction != null) return;
                if (actionTimer.hasElapsed(3000)) pendingAction = new BlockedFeature(WatchedPacketType.STOP_USING_ITEM, this);
                actionTimer.reset();
            } else if (packet instanceof C0BPacketEntityAction) {
                C0BPacketEntityAction.Action action = ((C0BPacketEntityAction) packet).getAction();
                if (pendingAction != null) return;
                WatchedPacketType type;
                switch (action) {
                    case START_SPRINTING: type = WatchedPacketType.START_SPRINTING; break;
                    case STOP_SNEAKING: type = WatchedPacketType.STOP_SNEAKING; break;
                    case START_SNEAKING: type = WatchedPacketType.START_SNEAKING; break;
                    default: return;
                }
                if (actionTimer.hasElapsed(3000)) pendingAction = new BlockedFeature(type, this);
                actionTimer.reset();
            }
            return;
        }
        if (applied) receiveTimer.reset();
        if (packet instanceof S14PacketEntity) return;
        if (packet instanceof S3APacketTabComplete) {
            if (!applied || !waitingTab) return;
            totalDelay += Math.min(500, Math.max(1, (int) (System.currentTimeMillis() - tabTimer.lastMs)));
            delayCount++;
            waitingTab = false;
        } else if (packet instanceof S23PacketBlockChange) {
            if (!applied || pendingBlock == null || placementTimer.hasElapsed(500)) return;
            if (!((S23PacketBlockChange) packet).getBlockPosition().equals(pendingBlock)) return;
            totalDelay += Math.min(500, Math.max(1, (int) (System.currentTimeMillis() - placementTimer.lastMs)));
            delayCount++;
            pendingBlock = null;
        } else if (packet instanceof S21PacketChunkData || packet instanceof S26PacketMapChunkBulk) {
            if (!applied) startClock();
        } else if (packet instanceof S1CPacketEntityMetadata) {
            if (!applied || pendingAction == null || mc.thePlayer == null) return;
            S1CPacketEntityMetadata metadata = (S1CPacketEntityMetadata) packet;
            if (metadata.getEntityId() != mc.thePlayer.getEntityId() || metadata.func_149376_c() == null) return;
            for (DataWatcher.WatchableObject object : metadata.func_149376_c()) {
                if (object.getDataValueId() != 0 || !(object.getObject() instanceof Byte)) continue;
                byte flags = (Byte) object.getObject();
                boolean match;
                switch ((WatchedPacketType) pendingAction.feature) {
                    case START_SNEAKING: match = (flags & 2) != 0; break;
                    case STOP_SNEAKING: match = (flags & 2) == 0; break;
                    case START_SPRINTING: match = (flags & 8) != 0; break;
                    case START_USING_ITEM: match = (flags & 16) != 0; break;
                    case STOP_USING_ITEM: match = (flags & 16) == 0; break;
                    default: match = false;
                }
                if (match) {
                    totalDelay += Math.min(500, Math.max(1, (int) (System.currentTimeMillis() - actionTimer.lastMs)));
                    delayCount++;
                    pendingAction = null;
                }
            }
        }
    }
    public void startClock() {
        if (chunks == 0) {
            if (thread != null) { thread.stopped = true; thread.interrupt(); }
            thread = new PacketWatcherThread(this);
            thread.start();
        }
        chunks++;
    }
}
