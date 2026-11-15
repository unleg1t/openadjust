package com.adjustclient.module.impl.combat;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.MouseKey;
import com.adjustclient.event.AttackInputStageEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.MouseDeltaEvent;
import com.adjustclient.event.InventoryKeyEvent;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.combat.autoblock.AutoBlockMode;
import com.adjustclient.module.impl.combat.autoblock.mode.PredictiveAutoBlockMode;
import com.adjustclient.module.impl.combat.displacement.DisplacementModule;
import com.adjustclient.module.util.CpsCounter;
import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.module.util.MouseClickHelper;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.TrackedLivingEntity;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;
import com.adjustclient.module.impl.combat.autoblock.AutoBlockModule;

public final class AutoClickerModule extends Module implements MinecraftInstance {
    public static AutoClickerModule INSTANCE;
    public final CpsSettings cps;
    public final ExpandSetting disableOn;
    public final BooleanSetting disableOnBreakingBlock;
    public final BooleanSetting disableOnBlockingSword;
    public final BooleanSetting disableOnNoWeapon;
    public final ExpandSetting inventory;
    public final BooleanSetting inventoryEnabled;
    public final BooleanSetting inventoryCpsOverride;
    public final ExpandSetting triggerBot;
    public final BooleanSetting triggerBotEnabled;
    public final ExpandSetting triggerBotTargets;
    public final BooleanSetting triggerBotTargetsPlayers;
    public final BooleanSetting triggerBotTargetsTeammates;
    public final BooleanSetting triggerBotTargetsInvisibles;
    public final KeybindSetting holdBind;
    private Timer inventoryTimer;
    private int lastSlot;
    public boolean handlingMouseInput;
    private boolean slotChanged;
    private boolean awaitMouseRelease;

    public AutoClickerModule() {
        super("Auto Clicker", Category.COMBAT);
        INSTANCE = this;
        cps = new CpsSettings(this, 8, 16);
        disableOn = new ExpandSetting(this, "Disable on");
        disableOnBreakingBlock = new BooleanSetting(disableOn, "Breaking block", true);
        disableOnBlockingSword = new BooleanSetting(disableOn, "Blocking sword", false);
        disableOnNoWeapon = new BooleanSetting(disableOn, "No weapon", true);
        inventory = new ExpandSetting(this, "Inventory");
        inventory.setDescription("Allows Auto Clicker to work in inventories");
        inventoryEnabled = new BooleanSetting(inventory, "Enabled", true, "Yes", "No");
        inventory.enabledPredicate = () -> this.inventoryEnabled.isEnabled();
        inventoryEnabled.setDescription("Allows Auto Clicker to work in inventories");
        inventoryCpsOverride = new BooleanSetting(inventory, "CPS override", true);
        inventoryCpsOverride.setDescription("Override CPS if your mouse moves over to another slot");
        triggerBot = new ExpandSetting(this, "Trigger Bot");
        triggerBot.setDescription("Automatically clicks when looking at an entity.");
        triggerBotEnabled = new BooleanSetting(triggerBot, "Enabled", false, "Yes", "No");
        triggerBot.enabledPredicate = () -> this.triggerBotEnabled.isEnabled();
        triggerBotTargets = new ExpandSetting(triggerBot, "Targets");
        triggerBotTargetsPlayers = new BooleanSetting(triggerBotTargets, "Players", true);
        triggerBotTargetsTeammates = new BooleanSetting(triggerBotTargets, "Teammates", false);
        triggerBotTargetsInvisibles = new BooleanSetting(triggerBotTargets, "Invisibles", true);
        lastSlot = 0;
        inventoryTimer = new Timer();
        Bind bind = Adjust.INSTANCE.bindManager.register(
                "module.autoclicker.holdbind", mode -> { }, "Auto Clicker Hold Bind");
        bind.addKey(new MouseKey(0));
        holdBind = new KeybindSetting(this, "Hold Bind", bind);
    }

    @EventHandler(priority = 49)
    public void onUseItemTick(IngameActionEvent event) {
        if (!isEnabled()) return;
        ItemStack stack = mc.thePlayer.getItemInUse();
        if (stack == null || stack.getItemUseAction() != EnumAction.BLOCK) return;
        var autoBlock = AutoBlockModule.INSTANCE;
        if (autoBlock.isEnabled()) {
            AutoBlockMode mode = autoBlock.mode.getValue();
            if (mode instanceof PredictiveAutoBlockMode && ((PredictiveAutoBlockMode) mode).isBlockingClick()) return;
        }
        if (disableOnBlockingSword.isEnabled()) return;
        if (cps.clickDelay == -1) cps.rollClickDelay();
        if (!Adjust.cpsCounter.canClick(cps.clickDelay, CpsCounter.Button.LEFT, 0)) return;
        if (canClick()) {
            Adjust.mouseClickHelper.right.disabled = true;
            mc.rightClickDelayTimer = 0;
        }
    }

    @EventHandler
    public void onMouseInput(com.adjustclient.event.ScreenMouseClickEvent event) {
        if (!isEnabled()) return;
        if (!(mc.currentScreen instanceof GuiContainer) || !inventoryEnabled.isEnabled()
                || !Mouse.isCreated() || !Mouse.isButtonDown(0) || awaitMouseRelease) {
            inventoryTimer.reset();
            return;
        }
        if (inventoryTimer.lastMs + 160L > System.currentTimeMillis()) return;
        boolean due = Adjust.cpsCounter.canClick(cps);
        boolean override = slotChanged && inventoryCpsOverride.isEnabled();
        if (!due && !override) return;
        awaitMouseRelease = true;
        Adjust.cpsCounter.recordClick();
        event.cancelled = true;
        handlingMouseInput = true;
        int oldButton = Mouse.eventButton;
        boolean oldState = Mouse.event_state;
        Mouse.eventButton = 0;
        Mouse.event_state = true;
        try {
            mc.currentScreen.handleMouseInput();
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Auto Clicker handle mouse input: {}", exception);
        }
        handlingMouseInput = false;
        Mouse.eventButton = oldButton;
        Mouse.event_state = oldState;
    }

    @EventHandler(priority = 100)
    public void onAttackTick(AttackInputStageEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        lastSlot = -1;
        slotChanged = false;
        if (!canClick()) return;
        var left = Adjust.mouseClickHelper.left;
        if (left.disabled) return;
        if (cps.clickDelay == -1) cps.rollClickDelay();
        if (!Adjust.cpsCounter.canClick(cps.clickDelay, CpsCounter.Button.LEFT, 0)) return;
        if (DisplacementModule.INSTANCE.tryDisplaceTarget()) return;
        int choices = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                ? 1 : 3;
        int swingDelay = RandomUtil.RANDOM.nextInt(choices);
        left.clicks = 1;
        left.down = Boolean.TRUE;
        left.holdTicks = swingDelay;
        Adjust.cpsCounter.recordClick();
    }

    @EventHandler
    public void onSlotRender(com.adjustclient.event.SlotRenderEvent event) {
        if (!isEnabled()) return;
        Slot slot = event.slot;
        if (slot == null) return;
        GuiContainer screen = event.screen;
        int mouseX = event.mouseX - screen.guiLeft;
        int mouseY = event.mouseY - screen.guiTop;
        if (slot.xDisplayPosition - 1 <= mouseX && slot.xDisplayPosition + 17 > mouseX
                && slot.yDisplayPosition - 1 <= mouseY && slot.yDisplayPosition + 17 > mouseY) {
            int previous = lastSlot;
            lastSlot = slot.slotNumber;
            if (lastSlot != previous) slotChanged = true;
        }
    }

    @EventHandler
    public void onPlayerUpdate(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (isEnabled() && !event.isCancelled()) awaitMouseRelease = false;
    }

    @Override public void onEnable() {
        lastSlot = -1;
        slotChanged = false;
        awaitMouseRelease = true;
        inventoryTimer = new Timer();
    }

    public boolean canClick() {
        if (mc.currentScreen != null) return false;
        var screens = Adjust.INSTANCE.screenStack.overlays;
        if (!screens.isEmpty() && screens.get(screens.size() - 1) != null) return false;
        if (Adjust.inventoryStateTracker.open) return false;
        if (disableOnBreakingBlock.isEnabled() && mc.playerController.getIsHittingBlock()) return false;
        if (disableOnNoWeapon.isEnabled() && !WorldUtil.isHoldingWeapon()) return false;
        if (!ServerTracker.INSTANCE.hasSwingCooldownElapsed()) return false;
        var game = Adjust.gameStateTracker.state;
        if ((game.game.flags & 2) != 0) return false;
        if (triggerBotEnabled.isEnabled() && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            var entity = ServerTracker.INSTANCE.entityTracker.get(mc.objectMouseOver.entityHit);
            if (entity instanceof TrackedLivingEntity) {
                boolean players = triggerBotTargetsPlayers.isEnabled();
                boolean teammates = triggerBotTargetsTeammates.isEnabled();
                boolean invisibles = triggerBotTargetsInvisibles.isEnabled();
                int mask = (players ? 2 : 0) | (teammates ? 0x20 : 0) | (invisibles ? 0x40 : 0) | 0x400;
                if (entity.matches(mask) || ((TrackedLivingEntity) entity).matchesType(mask)) return true;
            }
        }
        return holdBind.isDown();
    }
}
