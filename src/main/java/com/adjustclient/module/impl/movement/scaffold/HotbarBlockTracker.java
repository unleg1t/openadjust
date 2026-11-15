package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import com.adjustclient.util.anticheat.ServerTracker;

public final class HotbarBlockTracker implements MinecraftInstance {

    /** hotbar slot (0-8) -> predicted stack size */
    private final Int2IntOpenHashMap predictedCounts = new Int2IntOpenHashMap();
    /** hotbar slot (0-8) -> time of the last prediction update of that slot */
    private final Int2ObjectOpenHashMap<Timer> slotTimers = new Int2ObjectOpenHashMap<>();
    /** slot whose next server correction is accepted unconditionally (see {@link #onSetSlot}) */
    Integer forcedSlot;

    /** Lowers the predicted stack size of the held slot by one. */
    public void onBlockPlaced() {
        int slot = mc.thePlayer.inventory.m();
        ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
        if (stack == null) {
            return;
        }
        int predicted = this.predictedCounts.getOrDefault(slot, stack.stackSize + 1) - 1;
        Timer timer = this.slotTimers.computeIfAbsent(slot, (Int2ObjectFunction<Timer>) k -> new Timer(0L));

        long delay = 1000;
        com.adjustclient.util.anticheat.blocker.BlockerState latencyOwner = ServerTracker.INSTANCE.serverState;
        if (latencyOwner != null) {
            com.adjustclient.util.anticheat.blocker.PacketWatcher accumulator = latencyOwner.packetWatcher;
            if (accumulator.delayCount != 0) {
                delay = accumulator.totalDelay / accumulator.delayCount + 1000L;
            }
        }
        // trust the real stack size again when the slot has not been used for a while
        int updated = timer.hasElapsed(delay) ? stack.stackSize : predicted;
        this.predictedCounts.put(slot, updated);
        timer.reset();
    }

    /**
     * Whether the stack in {@code slot} is, according to the prediction, already used up while the server still reports
     * (almost) the same stack. Forgets all predictions when the prediction is far off.
     */
    public boolean isPredictedEmpty(int slot, ItemStack stack) {
        int actual = stack.stackSize;
        int predicted = this.predictedCounts.getOrDefault(slot, actual);
        if (predicted >= 1 || actual < 1) {
            return false;
        }
        int tracked = this.predictedCounts.get(slot);
        if (Math.abs(tracked - actual) < 9) {
            return true;
        }
        this.predictedCounts.clear();
        return false;
    }

    /** Predicted number of blocks Scaffold can place from the hotbar (sums the valid block stacks of slots 0-8). */
    public int getTotalBlocks() {
        ItemStack[] hotbar = mc.thePlayer.inventory.mainInventory;
        ItemStack first = hotbar[0];
        int total;
        if (first != null && first.getItem() instanceof ItemBlock
                && ScaffoldModule.INSTANCE.isUsableBlock(0, first)) {
            total = this.predictedCounts.getOrDefault(0, first.stackSize);
        } else {
            total = 0;
        }
        for (int slot = 1; slot < 9; slot++) {
            ItemStack stack = hotbar[slot];
            if (stack != null && stack.getItem() instanceof ItemBlock
                    && ScaffoldModule.INSTANCE.isUsableBlock(slot, stack)) {
                total += this.predictedCounts.getOrDefault(slot, stack.stackSize);
            }
        }
        return total;
    }

    /** Applies a server slot update (only hotbar slots 36-44 of the player container are tracked). */
    public void onSetSlot(S2FPacketSetSlot packet) {
        int slot = packet.slot - 36;
        if (slot < 0 || slot >= 9) {
            return;
        }
        ItemStack item = packet.item;
        if (item == null || !(item.getItem() instanceof ItemBlock)) {
            this.predictedCounts.remove(slot);
            return;
        }
        ItemStack current = mc.thePlayer.inventory.mainInventory[slot];
        if (current == null) {
            return;
        }
        int serverCount = item.stackSize;
        if (this.forcedSlot != null && this.forcedSlot == slot) {
            this.forcedSlot = null;
        } else if (Math.abs(serverCount - current.stackSize) < 9) {
            // the server value is close to what the client already shows: keep the prediction
            return;
        }
        this.predictedCounts.put(slot, serverCount);
    }
}
