package com.adjustclient.game.player;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.module.impl.movement.inventorymove.LegitInventoryMoveMode;
import com.adjustclient.service.party.PartyHudScreen;
import com.adjustclient.ui.stats.HypixelStatsScreen;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.ScreenUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;

public final class InventoryStateTracker implements Listener, MinecraftInstance {
    public static boolean inServerGui;
    public static boolean inServerInventory;
    public final IntOpenHashSet suppressedKeys = new IntOpenHashSet();
    public State state;
    public Object field12;
    public int field16;
    public boolean open;
    public boolean field21;
    public boolean suppressSneak;
    private Object field24;

    public enum State {
        OPENED, CLOSED;
        private Object field16;
    }

    public static boolean isInServersideGui() {
        return (inServerGui || inServerInventory) && !InventoryMoveModule.INSTANCE.canInteractInScreen(false);
    }

    @EventHandler public void onKeyPress(com.adjustclient.event.KeyBindingStateEvent event) {
        KeyBinding key = event.binding;
        boolean consume = event.queuedPress;
        if (suppressedKeys.contains(key.getKeyCode()) || inServerInventory && !filterKey(key, consume)) {
            if (!consume) event.pressed = false;
            else event.cancelled = true;
        }
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (event.sneak || !open && !suppressSneak) return;
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) { suppressSneak = false; return; }
        if (mc.currentScreen == null && !ScreenUtil.isAdjustScreenOpen()) {
            event.sneak = true; return;
        }
        InventoryMoveModule module = InventoryMoveModule.INSTANCE;
        if (!module.isEnabled() || !module.keyboardSneak.isEnabled()) {
            suppressSneak = false; return;
        }
        if (module.keyboardSneak.hideIf != null
                && module.keyboardSneak.hideIf.getAsBoolean()) {
            suppressSneak = false; return;
        }
        event.sneak = true;
    }

    public boolean filterKey(KeyBinding key, boolean consume) {
        var settings = mc.gameSettings;
        if (key == settings.keyBindAttack || key == settings.keyBindUseItem) {
            if (consume) suppressedKeys.add(key.getKeyCode());
            return true;
        }
        return key == settings.keyBindInventory || key == settings.keyBindDrop
                || key == settings.keyBindChat || key == settings.keyBindCommand;
    }

    @EventHandler public void onPacketReceive(LowLevelPacketReceiveEvent event) {
        Object packet = event.packet;
        if (packet instanceof S2EPacketCloseWindow || packet instanceof S2DPacketOpenWindow
                || packet instanceof S08PacketPlayerPosLook && (mc.thePlayer == null
                || !mc.thePlayer.sendQueue.doneLoadingTerrain)) {
            open = false; field16 = 0; updateKeys();
        }
    }

    @EventHandler public void onPacketSend(DirectionalPacketEvent event) {
        if (event.isCancelled() || !event.outgoing) return;
        Object packet = event.packet;
        if (packet instanceof C16PacketClientStatus status && status.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
            open = true; updateKeys();
        } else if (packet instanceof C0DPacketCloseWindow) {
            open = false; field16 = 0; updateKeys();
        }
    }

    public static void updateKeys() {
        InventoryMoveModule module = InventoryMoveModule.INSTANCE;
        if (!module.isEnabled()) return;
        GuiScreen screen = mc.currentScreen;
        Object mode = module.mode.getValue();
        if (mode != null && mode.getClass() == LegitInventoryMoveMode.class
                && screen instanceof GuiContainer && screen.getClass() != GuiInventory.class) return;
        Object overlay = Adjust.INSTANCE.screenStack.getTopOverlay();
        if (overlay == PartyHudScreen.INSTANCE) return;
        if (screen != null && (screen.getClass() == GuiDownloadTerrain.class || screen.getClass() == GuiControls.class
                || screen.getClass() == HypixelStatsScreen.class || screen.getClass() == GuiScreenResourcePacks.class)) return;
        if (ScreenUtil.canMoveInScreen()) return;
        boolean allowInventory = module.clientsideOnly.isEnabled();
        if (allowInventory && (inServerGui || inServerInventory)) return;
        boolean block;
        if (inServerGui || inServerInventory || ScreenUtil.isSettingsScreenOpen())
            block = !mc.isGamePaused;
        else if (screen != null && screen.getClass() == GuiInventory.class) block = !allowInventory;
        else block = overlay != null;
        if (!block) return;
        for (KeyBinding key : InventoryMoveModule.MOVEMENT_AND_JUMP_KEYS) InventoryMoveModule.pollKeyIfEnabled(key);
    }
}
