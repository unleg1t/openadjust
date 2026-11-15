package com.adjustclient.module.impl.player.fastuse.feature;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.module.impl.player.fastuse.FastUseFeature;
import com.adjustclient.module.impl.player.fastuse.FastUseModule;
import com.adjustclient.module.impl.player.inventory.item.type.BlockItemType;
import com.adjustclient.property.KeybindManager;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.BlockBreakUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public final class AutomationFeature extends FastUseFeature {
    public final BooleanSetting autoTool;
    public final KeybindSetting autoToolBind;
    public final BooleanSetting blocks;
    public final ModeSetting<ChangeSlotMode> changeSlot;
    public static AutomationFeature INSTANCE;
    public final com.adjustclient.util.Timer useTimer = new com.adjustclient.util.Timer();
    public final com.adjustclient.util.Timer switchTimer = new com.adjustclient.util.Timer();
    public int previousSlot = -1;
    public int toolSlot = -1;
    public int previousRenderSlot = -1;
    public int randomDelay;
    public boolean switched;

    public AutomationFeature(FastUseModule module) {
        super(module);
        INSTANCE = this;
        ExpandSetting group = new ExpandSetting(module, "Automation");
        group.enabledPredicate = () -> isFeatureEnabled();
        autoTool = new BooleanSetting(group, "Auto Tool", true, "On", "Off");
        autoTool.setDisplayName("Auto tool");
        KeybindManager manager = com.adjustclient.Adjust.INSTANCE.bindManager;
        Bind bind = manager.register("module.fastuse.autotoolbind", null, "Auto Tool Bind");
        autoToolBind = new KeybindSetting(group, "Auto Tool Bind", bind, () -> !autoTool.isEnabled());
        blocks = new BooleanSetting(group, "Blocks", true, "On", "Off",
                () -> !autoTool.isEnabled());
        changeSlot = new ModeSetting<>(group, "Change slot", ChangeSlotMode.CLIENT,
                ChangeSlotMode.CLIENT, ChangeSlotMode.SILENT);
    }

    @Override public void onDisable() {
        previousSlot = -1;
        toolSlot = -1;
        previousRenderSlot = -1;
        switched = false;
    }

    public void onTick() {
        if (!isActive()) return;
        if (!Adjust.mouseClickHelper.left.swing
                && Adjust.mouseClickHelper.left.key.pressTime < 1) return;
        if (Adjust.slotSpoofer.active && Adjust.slotSpoofer.field17) return;
        int realSlot = mc.thePlayer.inventory.currentItem;
        boolean manualSwitch = previousRenderSlot != -1 && previousRenderSlot != realSlot
                || mc.gameSettings.keyBindsHotbar[realSlot].isKeyDown();
        previousRenderSlot = realSlot;
        if (switched && (!Adjust.mouseClickHelper.right.isClicked(false) || useTimer.hasElapsed(1169L))) switched = false;
        if (updateSelectedSlot()) {
            randomDelay = com.adjustclient.util.RandomUtil.nextInt(269);
            switchTimer.reset();
            ItemStack current = mc.thePlayer.getHeldItem();
            if (current != null && current.getItem() instanceof ItemBlock) {
                randomDelay += 20;
                ItemStack rendered = Adjust.slotSpoofer.getRenderItemStack();
                if (rendered != null && rendered.getItem() != null && rendered.getItem().getClass() == net.minecraft.item.ItemSword.class) randomDelay += 100;
                if (mc.thePlayer.rotationPitch > 72.0F) randomDelay += mc.thePlayer.isSneaking() ? 270 : 95;
                if (mc.thePlayer.isSneaking()) randomDelay += 170;
            }
        }
        if (previousSlot != -1) toolSlot = previousSlot;
        else if (!keepBlockSlot()) {
            net.minecraft.util.MovingObjectPosition hit = mc.objectMouseOver;
            if (manualSwitch || isCombatBlockingOrTargeting() || switchTimer.hasElapsed(randomDelay + 222L)
                    || hit != null && (hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.ENTITY
                    || hit.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK
                    && Adjust.mouseClickHelper.right.isClicked(false))) {
                int old = toolSlot;
                toolSlot = -1;
                if (old != -1 && Adjust.slotSpoofer.active && old == mc.thePlayer.inventory.m()) {
                    Adjust.slotSpoofer.field17 = false;
                    Adjust.slotSpoofer.active = false;
                }
            }
        }
        if (toolSlot == -1) return;
        Adjust.slotSpoofer.slot = toolSlot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = false;
        Adjust.slotSpoofer.renderSpoofed = changeSlot.getValue() == ChangeSlotMode.CLIENT;
    }

    public static int findBestTool(net.minecraft.block.state.IBlockState state, net.minecraft.util.BlockPos pos) {
        net.minecraft.block.Block block = state.getBlock();
        int currentSlot = mc.thePlayer.inventory.currentItem;
        ItemStack held = mc.thePlayer.inventory.mainInventory[currentSlot];
        if (Adjust.gameStateTracker.state.game == com.adjustclient.hypixel.HypixelGame.BED_WARS
                && (block == Blocks.chest || block == Blocks.ender_chest)) return -1;
        if (Float.isInfinite(BlockBreakUtil.getRelativeHardness(block, pos, held))) return -1;
        float fastest = BlockBreakUtil.getDigSpeed(mc.thePlayer, block, held, true, false);
        int bestSlot = currentSlot;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack candidate = mc.thePlayer.inventory.mainInventory[slot];
            if (candidate == null) continue;
            if (mc.playerController.getCurrentGameType() == net.minecraft.world.WorldSettings.GameType.ADVENTURE
                    && !candidate.canDestroy(block)) continue;
            float speed = BlockBreakUtil.getDigSpeed(mc.thePlayer, block, candidate, true, false);
            if (speed > fastest) { fastest = speed; bestSlot = slot; }
        }
        return bestSlot;
    }

    @Override public boolean isFeatureEnabled() { return true; }

    private static boolean isCombatBlockingOrTargeting() {
        var ka = com.adjustclient.module.impl.combat.killaura.KillAuraModule.INSTANCE;
        if (ka != null && ka.isEnabled() && ka.active && ka.targetSelector != null
                && (ka.targetSelector.rotationTarget != null || ka.targetSelector.attackTarget != null || ka.canSwing())) {
            return true;
        }
        var ab = com.adjustclient.module.impl.combat.autoblock.AutoBlockModule.INSTANCE;
        if (ab != null && ab.isEnabled()) {
            int realSlot = mc.thePlayer.inventory.currentItem;
            ItemStack realStack = realSlot >= 0 && realSlot < 9 ? mc.thePlayer.inventory.mainInventory[realSlot] : null;
            if (realStack != null && realStack.getItem().getClass() == net.minecraft.item.ItemSword.class) {
                var mode = ab.mode.getValue();
                boolean predictiveBusy = mode instanceof com.adjustclient.module.impl.combat.autoblock.mode.PredictiveAutoBlockMode p
                        && p.state != com.adjustclient.module.impl.combat.autoblock.mode.AutoBlockState.IDLE;
                if (predictiveBusy || ab.hasThreat()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateSelectedSlot() {
        if (!autoTool.isEnabled() || !autoToolBind.getValue().isHeld()) { previousSlot = -1; return false; }
        if (mc.cameraOverride != null) { previousSlot = -1; return false; }
        if (blocks.isEnabled() && mc.gameSettings.keyBindUseItem.isKeyDown()
                && !isCombatBlockingOrTargeting()
                && !(com.adjustclient.rotation.handler.RotationHandler.INSTANCE.active
                && com.adjustclient.rotation.handler.RotationHandler.INSTANCE.requested)
                && !mc.gameSettings.keyBindAttack.isKeyDown()
                && mc.playerController.getCurrentGameType() != net.minecraft.world.WorldSettings.GameType.ADVENTURE) {
            ItemStack rendered = Adjust.slotSpoofer.getRenderItemStack();
            if (!com.adjustclient.util.WorldUtil.isPlaceableItem(rendered)) {
                if (com.adjustclient.util.WorldUtil.isUsableItem(rendered)) { switched = true; useTimer.reset(); }
                else if (!switched && mc.objectMouseOver != null
                        && mc.objectMouseOver.typeOfHit == net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
                    net.minecraft.util.BlockPos pos = mc.objectMouseOver.getBlockPos();
                    if (!com.adjustclient.util.BlockUtil.isInteractable(mc.theWorld.getBlockState(pos).getBlock())) {
                        for (int slot = 0; slot < 9; slot++) {
                            ItemStack candidate = mc.thePlayer.inventory.mainInventory[slot];
                            if (candidate == null || !(candidate.getItem() instanceof ItemBlock)) continue;
                            ItemBlock item = (ItemBlock) candidate.getItem();
                            if (BlockItemType.isValidBlock(item) && item.canPlaceBlockOnSide(mc.theWorld, pos,
                                    mc.objectMouseOver.sideHit, mc.thePlayer, candidate)) {
                                previousSlot = slot;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (mc.gameSettings.keyBindUseItem.isKeyDown() || !mc.gameSettings.keyBindAttack.isKeyDown()
                || mc.playerController.getCurrentGameType() != net.minecraft.world.WorldSettings.GameType.SURVIVAL
                || mc.objectMouseOver == null
                || mc.objectMouseOver.typeOfHit != net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK) {
            previousSlot = -1;
            return false;
        }
        net.minecraft.util.BlockPos pos = mc.objectMouseOver.getBlockPos();
        int best = findBestTool(mc.theWorld.getBlockState(pos), pos);
        if (best == -1) return false;
        previousSlot = best;
        return true;
    }

    public boolean keepBlockSlot() {
        if (!autoTool.isEnabled() || !autoToolBind.getValue().isHeld() || !blocks.isEnabled() || isCombatBlockingOrTargeting()) return false;
        if (!com.adjustclient.Adjust.mouseClickHelper.right.isClicked(false) || toolSlot < 0 || toolSlot >= 9) return false;
        ItemStack stack = mc.thePlayer.inventory.mainInventory[toolSlot];
        return stack != null && stack.getItem() instanceof ItemBlock
                && BlockItemType.isValidBlock((ItemBlock) stack.getItem());
    }

    public enum ChangeSlotMode implements ModeOption {
        CLIENT("Client"), SILENT("Silent");
        private final String name;
        ChangeSlotMode(String name) { this.name = name; }
        @Override public String getName() { return name; }
    }
}
