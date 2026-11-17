package com.adjustclient.module.impl.player;

import com.adjustclient.Adjust;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.player.inventory.item.type.BlockItemType;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.function.Function;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import com.adjustclient.game.player.InventoryStateTracker;

public final class BlockSwapModule extends Module implements MinecraftInstance {
    public static BlockSwapModule INSTANCE;
    public final BooleanSetting pitchCheck;
    public final BooleanSetting matchingOnly;
    private Item lastItem;
    private int lastSlot = -1;

    public BlockSwapModule() {
        super("Block Swap", Category.PLAYER);
        INSTANCE = this;
        pitchCheck = new BooleanSetting(this, "Pitch Check", false);
        pitchCheck.setDescription("Only activate when looking down.");
        matchingOnly = new BooleanSetting(this, "Matching Only", true);
        matchingOnly.setDescription("Only swap to the same block type.");
    }

    private boolean passesPitchCheck() {
        return !pitchCheck.isEnabled() || pitchCheck.isHidden() || mc.thePlayer.rotationPitch >= 72.0F;
    }

    private Integer findReplacement(int currentSlot) {
        Function<Integer, Boolean> matching = slot -> {
            if (slot == currentSlot) return false;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            return stack != null && stack.getItem() instanceof ItemBlock
                    && BlockItemType.isValidBlock((ItemBlock) stack.getItem()) && stack.getItem() == lastItem;
        };
        Integer sameItem = com.adjustclient.util.WorldUtil.findHotbarSlot(currentSlot, matching);
        if (sameItem != null) return sameItem;
        if (matchingOnly.isEnabled() && !matchingOnly.isHidden()) return null;
        Function<Integer, Boolean> anyBlock = slot -> {
            if (slot == currentSlot) return false;
            ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
            return stack != null && stack.getItem() instanceof ItemBlock
                    && BlockItemType.isValidBlock((ItemBlock) stack.getItem());
        };
        return com.adjustclient.util.WorldUtil.findHotbarSlot(currentSlot, anyBlock);
    }

    public void onTick() {
        if (!isEnabled() || mc.thePlayer == null || InventoryStateTracker.isInServersideGui()) return;
        if (!Adjust.mouseClickHelper.right.isClicked(false)) {
            lastItem = null;
            lastSlot = -1;
            return;
        }
        if (Adjust.slotSpoofer != null && Adjust.slotSpoofer.active && Adjust.slotSpoofer.field17 && !Adjust.slotSpoofer.renderSpoofed) return;
        if (!passesPitchCheck()) return;
        int slot = mc.thePlayer.inventory.m();
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        if (stack != null && stack.getItem() instanceof ItemBlock
                && !ServerTracker.INSTANCE.hotbarBlockTracker.isPredictedEmpty(slot, stack)) {
            lastItem = stack.getItem();
            lastSlot = slot;
            return;
        }
        if (lastItem == null || slot != lastSlot) return;
        Integer replacement = findReplacement(slot);
        if (replacement != null) mc.thePlayer.inventory.currentItem = replacement;
    }

    @Override public void onEnable() { lastItem = null; lastSlot = -1; }
    @Override public void onDisable() { lastItem = null; lastSlot = -1; }
    @Override public String getDescription() { return "Automatically swaps to a different stack of blocks when you run out."; }
}
