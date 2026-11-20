package com.adjustclient.modules.impl.player.inventory;
import com.adjustclient.Adjust;
import com.adjustclient.modules.impl.player.inventory.plan.InventorySnapshot;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public final class ChestTracker implements MinecraftInstance {
    public final Timer[] armorTimers = new Timer[4];
    public final Timer useTimer = new Timer();

    public void prepareArmorUse(java.util.function.Supplier<InventorySnapshot> inventory) {
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && hit.getBlockPos() != null) {
            Block block = mc.theWorld.getBlockState(hit.getBlockPos()).getBlock();
            if (BlockUtil.INTERACTABLE_BLOCKS.contains(block) || block instanceof BlockFenceGate
                    || block instanceof BlockDoor || block instanceof BlockFence) return;
        } else if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL
                && hit.entityHit instanceof EntityArmorStand) return;
        int slot = findArmorSlot(inventory);
        if (slot == -1) return;
        useTimer.reset();
        Adjust.slotSpoofer.slot = slot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = false;
        Adjust.slotSpoofer.renderSpoofed = false;
        var click = Adjust.mouseClickHelper.right;
        click.clicks = Integer.valueOf(1);
        click.down = Boolean.TRUE;
        click.holdTicks = 1;
    }

    public int findArmorSlot(java.util.function.Supplier<InventorySnapshot> inventory) {
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        ItemStack[] contents = mc.thePlayer.inventory.mainInventory;
        List<ItemStack> combined = null;
        for (int index = 0; index < armor.length; index++) {
            if (armor[index] != null) continue;
            int type = 3 - index;
            if (type < 0 || type >= 4) continue;
            if (armorTimers[type] != null) {
                if (!armorTimers[type].hasElapsed(1100L)) continue;
                armorTimers[type] = null;
            }
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = contents[slot];
                if (stack == null || !(stack.getItem() instanceof ItemArmor)
                        || ((ItemArmor) stack.getItem()).armorType != type) continue;
                if (combined == null) {
                    combined = new ArrayList<>(contents.length + armor.length);
                    for (ItemStack item : contents) if (item != null) combined.add(item);
                    for (ItemStack item : armor) if (item != null) combined.add(item);
                }
                if (inventory.get().keepChecker.isUseful(stack)) return slot;
            }
        }
        return -1;
    }
}
