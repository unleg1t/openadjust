package com.adjustclient.module.impl.player.autohypixel.feature;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.impl.player.autohypixel.AutoHypixelModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.Timer;
import net.minecraft.block.BlockColored;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.lwjgl.input.Keyboard;
import com.adjustclient.module.impl.player.inventory.InventoryModule;

public class AutoReportAccept extends AutoHypixelFeature {
    public static AutoReportAccept INSTANCE;
    public final BooleanSetting enabled;
    public PendingReport pending;
    public final Timer reportTimer = new Timer(0L);

    public AutoReportAccept(AutoHypixelModule owner) {
        super(owner);
        INSTANCE = this;
        enabled = new BooleanSetting(owner, "Auto Report Accept", true);
    }
    @Override public boolean isFeatureEnabled() { return enabled.isEnabled(); }

    @EventHandler(priority = 1) public void onKey(ScreenKeyEvent event) {
        if (!isActive() || pending == null || pending.stage >= 7) return;
        int key = event.keyCode;
        if (key == Keyboard.KEY_ESCAPE || key == mc.gameSettings.keyBindInventory.getKeyCode()) pending = null;
        else event.cancelled = true;
    }

    @EventHandler public void onTick(UserInputVerificationEvent event) {
        if (!isActive()) { pending = null; return; }
        PendingReport previous = pending;
        pending = null;
        if (mc.currentScreen == null || mc.thePlayer == null || !(mc.thePlayer.openContainer instanceof ContainerChest)
                || reportTimer.hasElapsed(6000L)) return;
        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        IInventory inventory = chest.getLowerChestInventory();
        if (inventory.getSizeInventory() != 27 || !inventory.getDisplayName().getUnformattedText().startsWith("Report ")) return;
        ItemStack accept = inventory.getStackInSlot(11);
        if (accept == null || !(accept.getItem() instanceof ItemBlock)
                || !(((ItemBlock) accept.getItem()).getBlock() instanceof BlockColored)
                || accept.getMetadata() != EnumDyeColor.GREEN.getMetadata()) return;
        ItemStack reject = inventory.getStackInSlot(15);
        if (reject == null || !(reject.getItem() instanceof ItemBlock)
                || !(((ItemBlock) reject.getItem()).getBlock() instanceof BlockColored)
                || reject.getMetadata() != EnumDyeColor.RED.getMetadata()) return;
        ItemStack skull = inventory.getStackInSlot(13);
        if (skull == null || !(skull.getItem() instanceof ItemSkull) || !skull.hasDisplayName()) return;
        String name = skull.getDisplayName();
        if (name.length() < 14 || !name.startsWith("§7/report §b")) return;
        PendingReport current = previous != null && previous.container == chest ? previous : new PendingReport(chest);
        com.adjustclient.module.impl.player.inventory.InventoryModule.INSTANCE.pauseCooldown.timer.reset();
        com.adjustclient.module.impl.player.inventory.InventoryModule.INSTANCE.pauseCooldown.durationMs = 300L;
        pending = current;
        if (!current.timer.hasElapsed(210L)) return;
        if (current.stage == 1) mc.playerController.windowClick(chest.windowId, 11, 0, 0, mc.thePlayer);
        else if (current.stage == 2) {
            mc.thePlayer.closeScreen();
            reportTimer.lastMs = 0L;
        }
        current.stage++;
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isActive() || !event.outgoing || !(event.packet instanceof C01PacketChatMessage)) return;
        String text = ((C01PacketChatMessage) event.packet).getMessage();
        for (String command : new String[] {"cr", "chatreport", "wdr", "watchdogreport", "report", "badname"}) {
            if (text.startsWith("/" + command + " ")) { reportTimer.reset(); return; }
        }
    }

    public static class PendingReport {
        final ContainerChest container;
        final Timer timer;
        public int stage;
        PendingReport(ContainerChest container) { this.container = container; this.timer = new Timer(); }
    }
}
