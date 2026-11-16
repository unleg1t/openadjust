package com.adjustclient.ui.screen;

import com.adjustclient.Adjust;
import com.adjustclient.bind.BindMode;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.ScreenKeyEvent;
import com.adjustclient.event.ScreenMouseClickEvent;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.Vec2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.GuiScreen; // used for the active vanilla-screen check
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.adjustclient.module.impl.player.inventory.quickbuy.QuickBuyListener;
import com.adjustclient.service.ScreenStack;

public abstract class AdjustScreen {
    public static ScaledResolution screenResolution;
    public boolean blocksGameInteraction;

    public static ScreenMouseClickEvent handleInput(int button, boolean pressed) {
        Minecraft mc = Minecraft.getMinecraft();
        int primaryIndex = UserConfiguration.scale.getInt();
        double primary = primaryIndex == 16 ? 1000.0 : (primaryIndex - 1.0) * 0.25 + 2.0;
        int secondaryIndex = UserConfiguration.secondaryScale.getInt();
        double secondary = secondaryIndex == 16 ? 1000.0 : (secondaryIndex - 1.0) * 0.25 + 2.0;
        int width;
        int height;
        if (primary == secondary) {
            ScaledResolution resolution = (ScaledResolution) screenResolution;
            width = resolution.getScaledWidth();
            height = resolution.getScaledHeight();
        } else {
            if (primary % 0.25 != 0.0) throw new RuntimeException("Only increments of 0.25 are supported!");
            double factor = 1.0;
            while (primary > factor && mc.displayWidth / (factor + 1.0) >= 320.0 && mc.displayHeight / (factor + 1.0) >= 240.0) factor += 0.25;
            factor = Math.max(2.0, factor);
            width = (int) Math.ceil(mc.displayWidth / factor);
            height = (int) Math.ceil(mc.displayHeight / factor);
        }
        ScreenMouseClickEvent event = new ScreenMouseClickEvent();
        event.inputAction = pressed ? BindMode.PRESSED : BindMode.RELEASED;
        event.mouseX = (float) Mouse.getEventX() * width / mc.displayWidth;
        event.mouseY = (float) height - (float) Mouse.getEventY() * height / mc.displayHeight - 1.0F;
        event.mouseButton = button;
        if (Adjust.movableManager != null && (mc.currentScreen != null
                || com.adjustclient.util.ScreenUtil.isAdjustScreenOpen() || Adjust.inventoryStateTracker.open)
                && !(Adjust.INSTANCE.screenStack.getTopOverlay() instanceof com.adjustclient.ui.dialog.DialogScreen)) {
            Adjust.movableManager.onMouseClick(event, Adjust.movableManager.findHovered(event));
        }
        com.adjustclient.module.impl.combat.AutoClickerModule clicker = com.adjustclient.module.impl.combat.AutoClickerModule.INSTANCE;
        if (!event.cancelled && clicker != null && clicker.isEnabled()) clicker.onMouseInput(event);
        com.adjustclient.module.impl.player.autohypixel.feature.AutoReportAccept report = com.adjustclient.module.impl.player.autohypixel.feature.AutoReportAccept.INSTANCE;
        if (!event.cancelled && report != null && report.isActive()
                && report.pending != null && report.pending.stage < 7) event.cancelled = true;
        com.adjustclient.module.impl.player.inventory.InventoryModule inventory = com.adjustclient.module.impl.player.inventory.InventoryModule.INSTANCE;
        if (!event.cancelled && inventory != null && inventory.isEnabled()) inventory.onMouseClick(event);
        if (!event.cancelled && QuickBuyListener.INSTANCE != null && QuickBuyListener.INSTANCE.isActive())
            QuickBuyListener.INSTANCE.onMouse(event);
        if (!event.cancelled && HUDModule.INSTANCE != null && HUDModule.INSTANCE.isEnabled()) HUDModule.INSTANCE.onMouse(event);
        com.adjustclient.service.party.PartyOverlay party = com.adjustclient.service.party.PartyOverlay.INSTANCE;
        if (!event.cancelled && party != null && (party.service.party != null || !party.service.invitations.isEmpty()))
            party.onMouse(event);
        return event;
    }
    public void onClose(Vec2f mouse, int button, boolean pressed, int wheel, float direction) { }

    public void mouseScrolled(Vec2f mouse, int button, boolean pressed, int wheel, float direction) {
        onClose(mouse, button, pressed, wheel, direction);
    }

    public void render(Object window, float mouseX, float mouseY) {
        int button = Mouse.getEventButton();
        boolean pressed = Mouse.getEventButtonState();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) mouseScrolled(new Vec2f(mouseX, mouseY), button, pressed, wheel, wheel < 0 ? 1.0F : -1.0F);
        if (button != -1 && !handleInput(button, pressed).cancelled) {
            ScaledResolution resolution = window instanceof ScaledResolution
                    ? (ScaledResolution) window
                    : new ScaledResolution(Minecraft.getMinecraft(), HUDModule.getClickGuiScale());
            mouseClicked(new Vec2f(mouseX, mouseY), button, pressed, resolution);
        }
    }
    public boolean keyTyped(char character, int key) { return false; }
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed) { return false; }
    public boolean mouseClicked(Vec2f mouse, int button, boolean pressed, ScaledResolution window) {
        return mouseClicked(mouse, button, pressed);
    }
    public void render(Object queue, Vec2f mouse, ScaledResolution window) { }
    public void updateScreen() { }
    public void onOpen() { }
    public void onAdded() { }
    public boolean handleKeyboardEvent() {
        return Keyboard.getEventKeyState() && keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
    }
    public void tickInput() {
        if (Mouse.isCreated()) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc, HUDModule.getClickGuiScale());
            float x = (float) Mouse.getX() * resolution.getScaledWidth() / mc.displayWidth;
            float y = resolution.getScaledHeight() - (float) Mouse.getY() * resolution.getScaledHeight() / mc.displayHeight - 1.0F;
            while (Mouse.next()) render(resolution, x, y);
        }
        if (!Keyboard.isCreated()) return;
        while (Keyboard.next()) {
            if (handleKeyboardEvent() || !Keyboard.getEventKeyState()) continue;
            int code = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
            if (QuickBuyListener.INSTANCE != null && QuickBuyListener.INSTANCE.isActive()) QuickBuyListener.INSTANCE.onKey(new com.adjustclient.event.InventoryKeyEvent(code));
            ScreenKeyEvent down = new ScreenKeyEvent(true, (short) Keyboard.getEventCharacter(), Keyboard.getEventKey());
            EventBus.post(down);
            ScreenKeyEvent up = new ScreenKeyEvent(false, (short) Keyboard.getEventCharacter(), Keyboard.getEventKey());
            EventBus.post(up);
        }
    }
    public void onClose() { }
    public void close() {
        if (ScreenStack.deferOverlayChanges) Adjust.INSTANCE.screenStack.removeOverlay(this);
        else Adjust.INSTANCE.screenStack.closeOverlay(this);
    }
}
