package com.adjustclient.module.impl.movement.inventorymove;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.party.PartyHudScreen;
import com.adjustclient.ui.stats.HypixelStatsScreen;
import com.adjustclient.util.ScreenUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.Objects;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.adjustclient.backend.transport.HydraMessageDispatcher;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.module.impl.combat.killaura.KillAuraModule;

public final class InventoryMoveModule extends Module {
    public static InventoryMoveModule INSTANCE;
    public static InventoryMoveModule listenerInstance;
    public static LegitInventoryMoveMode legitMode;
    public static final KeyBinding[] MOVEMENT_KEYS;
    public static final KeyBinding[] MOVEMENT_AND_JUMP_KEYS;
    public final ModeSetting<InventoryMoveMode> mode;
    public final BooleanSetting keyboardMovement;
    public final BooleanSetting keyboardSprint;
    public final BooleanSetting keyboardJump;
    public final BooleanSetting keyboardSneak;
    public final BooleanSetting keyboardKeybinds;
    public final BooleanSetting clientsideOnly;
    public final BooleanSetting allowInteractions;
    public boolean movingInScreen;

    static {
        MOVEMENT_KEYS = new KeyBinding[] { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight };
        MOVEMENT_AND_JUMP_KEYS = new KeyBinding[] { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump };
    }

    public InventoryMoveModule() {
        super("Inventory Move", Category.MOVEMENT);
        INSTANCE = this;
        listenerInstance = this;
        clientsideOnly = new BooleanSetting(this, "Clientside only", false);
        clientsideOnly.addListener((oldValue, newValue) -> {
            Runnable reset = () -> resetKeys();
            reset.run();
        });
        allowInteractions = new BooleanSetting(this, "Allow interactions", false);
        legitMode = new LegitInventoryMoveMode(this);
        mode = new ModeSetting<>(this, "Mode", VanillaInventoryMoveMode.VANILLA, VanillaInventoryMoveMode.VANILLA, legitMode);
        clientsideOnly.hideIf = () -> mode.getValue() != VanillaInventoryMoveMode.VANILLA;
        allowInteractions.hideIf = () -> mode.getValue() != VanillaInventoryMoveMode.VANILLA || clientsideOnly.isEnabled();
        settings.remove(mode);
        settings.add(0, mode);
        ExpandSetting keyboard = new ExpandSetting(this, "Keyboard");
        keyboardMovement = new BooleanSetting(keyboard, "Movement", true);
        keyboardSprint = new BooleanSetting(keyboard, "Sprint", true);
        keyboardJump = new BooleanSetting(keyboard, "Jump", true);
        keyboardSneak = new BooleanSetting(keyboard, "Sneak", false);
        keyboardKeybinds = new BooleanSetting(keyboard, "Keybinds", true);
        legitMode.stopTime.setHideIf(() -> mode.getValue() != null && mode.getValue().getClass() == LegitInventoryMoveMode.class);
    }

    public void updateKeys() {
        if (keyboardMovement.isEnabled()) {
            for (KeyBinding key : MOVEMENT_KEYS) pollKey(key);
        }
        var state = ServerTracker.INSTANCE.serverState;
        boolean hypixel = state != null && state.antiCheatModel != null
                && state.antiCheatModel.serverType == ServerType.HYPIXEL;
        if (hypixel && (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory)) {
            mc.gameSettings.keyBindSneak.pressTime = 0;
            mc.gameSettings.keyBindSneak.pressed = false;
        } else if (keyboardSneak.isEnabled()) {
            pollKey(mc.gameSettings.keyBindSneak);
        }
        if (keyboardJump.isEnabled()) pollKey(mc.gameSettings.keyBindJump);
        if (keyboardSprint.isEnabled()) pollKey(mc.gameSettings.keyBindSprint);
    }

    public boolean canInteractInScreen(boolean checkKillAura) {
        if (checkKillAura && KillAuraModule.INSTANCE.active) return false;
        return isEnabled()
                && mode.getValue() == VanillaInventoryMoveMode.VANILLA
                && !clientsideOnly.isEnabled()
                && allowInteractions.isEnabled();
    }

    public boolean shouldHandleScreen(boolean keybind) {
        GuiScreen screen = mc.currentScreen;
        if (mode.getValue() instanceof LegitInventoryMoveMode && screen instanceof GuiContainer
                && screen.getClass() != GuiInventory.class) return false;
        com.adjustclient.ui.screen.AdjustScreen overlay = Adjust.INSTANCE.screenStack.getTopOverlay();
        if (overlay == PartyHudScreen.INSTANCE) return false;
        if (screen != null && (screen.getClass() == GuiDownloadTerrain.class || screen.getClass() == GuiControls.class
                || screen.getClass() == HypixelStatsScreen.class || screen.getClass() == GuiScreenResourcePacks.class)) return false;
        if (ScreenUtil.canMoveInScreen()) return false;
        if (!keybind && clientsideOnly.isEnabled() && (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory)) return false;
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory || ScreenUtil.isSettingsScreenOpen()) return !mc.isGamePaused;
        if (screen != null && screen.getClass() == GuiInventory.class) return keybind || !clientsideOnly.isEnabled();
        return overlay != null;
    }

    @EventHandler public void onUserInput(UserInputVerificationEvent event) {
        if (isEnabled() && canInteractInScreen(false)) {
            event.inputRequested = true;
            event.serverInventoryOpen = Boolean.FALSE;
        }
    }

    @EventHandler public void onInputTick(PostInputEvent event) {
        if (!isEnabled()) { movingInScreen = false; return; }
        if (mc.thePlayer == null) return;
        if (shouldHandleScreen(false)) {
            movingInScreen = true;
            updateKeys();
        } else if (movingInScreen) {
            movingInScreen = false;
            if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory || mc.currentScreen != null
                    || Adjust.INSTANCE.screenStack.hasBlockingOverlay()) KeyBinding.unPressAllKeys();
            else updateKeys();
        }
    }

    @EventHandler public void onKey(ScreenKeyEvent event) {
        if (!isEnabled() || event.pressed || !shouldHandleScreen(true) || !keyboardKeybinds.isEnabled()) return;
        KeyBindingUpdateEvent update = new KeyBindingUpdateEvent(event.keyCode);
        EventBus.post(update);
    }

    @SuppressWarnings("unused")
    public void vendorOnKey(ScreenKeyEvent event) {
        if (!isEnabled() || event.pressed || !shouldHandleScreen(true) || !keyboardKeybinds.isEnabled()) return;
        KeyBindingUpdateEvent update = new KeyBindingUpdateEvent(event.keyCode - HydraMessageDispatcher.inputKeyPayload.value + 32);
        EventBus.post(update);
    }

    public static void pollKey(KeyBinding key) {
        int code = key.getKeyCode();
        boolean pressed = code != 0 && code >= -100 && code < 256
                && (code < 0 ? Mouse.isButtonDown(code + 100) : Keyboard.isKeyDown(code));
        if (!pressed) key.pressTime = 0;
        key.pressed = pressed;
    }

    @Override public void onEnable() { InventoryMoveMode selected = mode.getValue(); }

    @Override public void onDisable() {
        if (mode.getValue() instanceof LegitInventoryMoveMode legit) {
            legit.inventoryAction = false;
            legit.releaseHold();
        }
        resetKeys();
    }

    public void resetKeys() {
        movingInScreen = false;
        if (shouldHandleScreen(false)) KeyBinding.unPressAllKeys();
    }

    public static void pollKeyIfEnabled(KeyBinding key) {
        if (INSTANCE.isEnabled()) pollKey(key);
        else {
            key.pressTime = 0;
            key.pressed = false;
        }
    }
}
