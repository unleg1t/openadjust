package com.adjustclient.module.impl.player;
import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.MovingObjectPosition;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
public final class FastPlaceModule extends Module {
    public static FastPlaceModule INSTANCE;
    public final BooleanSetting blocksOnly;
    public final BooleanSetting legit;
    public final NumberSetting activationDelay;
    public final Timer activationTimer = new Timer();
    public FastPlaceModule() {
        super("Fast Place", Category.PLAYER);
        INSTANCE = this;
        blocksOnly = new BooleanSetting(this, "Blocks only", true);
        blocksOnly.setDescription("Enable fast placing only if you are holding a block.");
        legit = new BooleanSetting(this, "Legit", true);
        legit.setDescription("Lowers your click count without lowering block places.");
        activationDelay = new NumberSetting(this, "Activation delay", 100, 0, 500, 50);
        activationDelay.setDescription("Time to wait before fast placing.");
        activationDelay.formatter(NumberSetting.MILLISECONDS);
    }
    public boolean hasActivated() {
        var right = Adjust.mouseClickHelper.right;
        if (!right.disabled && (right.clicks != null ? right.clicks > 0 : right.key.pressTime > 0)) activationTimer.reset();
        long delay = activationDelay.getLong();
        return delay < 1L || activationTimer.hasElapsed(delay);
    }
    @EventHandler(priority = 91) public void onUpdate(com.adjustclient.event.IngameActionEvent event) {
        if (event.suppressActions || !isEnabled() || mc.thePlayer == null) return;
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        if (!hasActivated() || !isAllowedItem(stack)) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        boolean blockHit = hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
        if (blockHit && BlockUtil.isInteractable(mc.theWorld.getBlockState(hit.getBlockPos()).getBlock())) return;
        if (blocksOnly.isEnabled() && !WorldUtil.isPlaceableItem(stack)) return;
        if (mc.cameraOverride != null) return;
        if (!legit.isEnabled()) { mc.rightClickDelayTimer = 0; return; }
        if (Boolean.TRUE.equals(ScaffoldModule.INSTANCE.placeReady)) return;
        var right = Adjust.mouseClickHelper.right;
        if (right.disabled || !mc.gameSettings.keyBindUseItem.isKeyDown() || !blockHit
                || stack == null || !(stack.getItem() instanceof ItemBlock)) return;
        ItemBlock item = (ItemBlock)stack.getItem();
        if (item.canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit, mc.thePlayer, stack)) {
            right.clicks = 1;
            right.down = true;
        }
    }
    public boolean isAllowedItem(ItemStack stack) {
        var state = ServerTracker.INSTANCE.serverState;
        if (state == null || state.antiCheatModel == null
                || state.antiCheatModel.serverType != ServerType.HYPIXEL) return true;
        if (stack != null && stack.getItem() == Items.bed
                && stack.getDisplayName().contains("§c§l")
                && Boolean.FALSE.equals(Adjust.gameStateTracker.state.lobby) && Adjust.gameStateTracker.state.startTimer == null) return false;
        return (Adjust.gameStateTracker.state.game.flags & 2) == 0;
    }
}
