package com.adjustclient.modules.impl.player.inventory.quickbuy;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.ScreenUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerChest;
import org.lwjgl.input.Mouse;
import com.adjustclient.modules.impl.player.inventory.InventoryModule;

public final class QuickBuyListener implements Listener, MinecraftInstance {
    public static QuickBuyListener INSTANCE;
    public final QuickBuySettings settings;
    public final QuickBuyHandler handler;

    public QuickBuyListener(com.adjustclient.modules.impl.player.inventory.InventoryModule inventory) {
        INSTANCE = this;
        settings = inventory.inventorySettings.quickBuy;
        handler = new QuickBuyHandler(settings);
    }

    private boolean isGameKey(int code) {
        for (KeyBinding binding : mc.gameSettings.keyBindings)
            if (binding.getKeyCode() == code) return true;
        return false;
    }

    @EventHandler public void onScreenKey(ScreenKeyEvent event) {
        if (!isActive() || mc.thePlayer == null || !event.pressed || ScreenUtil.canMoveInScreen()) return;
        ContainerChest chest = mc.currentScreen instanceof GuiChest && mc.thePlayer.openContainer instanceof ContainerChest
                ? (ContainerChest) mc.thePlayer.openContainer : null;
        ShopCategory category = findCategory(chest);
        if (category == null || chest.windowId == 0 || settings.findKeyboard(event.keyCode, category) == null) return;
        if (isGameKey(event.keyCode)) event.cancelled = true;
    }

    @EventHandler public void onMouse(ScreenMouseClickEvent event) {
        if (!isActive() || mc.thePlayer == null) return;
        ContainerChest chest = mc.currentScreen instanceof GuiChest && mc.thePlayer.openContainer instanceof ContainerChest
                ? (ContainerChest) mc.thePlayer.openContainer : null;
        ShopCategory category = findCategory(chest);
        if (category == null || chest.windowId == 0) return;
        QuickBuyItem item = settings.findScroll(Mouse.getEventDWheel(), category);
        if (item != null) { handler.enqueue(item); event.cancelled = true; }
    }

    @EventHandler public void onScreenClose(GuiClosedEvent event) {
        if (!isActive() || !(event.screen instanceof GuiChest)) handler.reset();
    }

    @EventHandler public void onTick(UserInputVerificationEvent event) {
        if (!isActive() || mc.thePlayer == null || !(mc.currentScreen instanceof GuiChest)
                || !(mc.thePlayer.openContainer instanceof ContainerChest)
                || mc.thePlayer.openContainer.windowId == 0) {
            handler.reset(); return;
        }
        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
        for (ShopCategory category : ShopCategory.values()) {
            if (category.title.equals(title)) { handler.update(chest, category); return; }
        }
        handler.reset();
    }

    @EventHandler public void onKey(InventoryKeyEvent event) {
        if (!isActive() || mc.thePlayer == null || ScreenUtil.canMoveInScreen()) return;
        ContainerChest chest = mc.currentScreen instanceof GuiChest && mc.thePlayer.openContainer instanceof ContainerChest
                ? (ContainerChest) mc.thePlayer.openContainer : null;
        ShopCategory category = findCategory(chest);
        if (category == null || chest.windowId == 0) return;
        int code = event.keyCode;
        QuickBuyItem item = settings.findKeyboard(code, category);
        if (item == null) return;
        handler.enqueueKey(item);
        if (isGameKey(code)) event.cancelled = true;
    }

    private ShopCategory findCategory(ContainerChest chest) {
        if (chest == null) return null;
        String title = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
        for (ShopCategory category : ShopCategory.values())
            if (category.title.equals(title)) return category;
        return null;
    }

    public boolean isActive() {
        if (!InventoryModule.INSTANCE.isEnabled() || !settings.enabled.isEnabled()) return false;
        var server = ServerTracker.INSTANCE.serverState;
        return server != null && server.antiCheatModel != null
                && server.antiCheatModel.serverType == ServerType.HYPIXEL
                && Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS;
    }
}
