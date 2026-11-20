package com.adjustclient.modules.impl.player;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.DirectionalPacketEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.PreClientTickEvent;
import com.adjustclient.utilities.game.packet.HeldPacketQueue;
import com.adjustclient.utilities.game.packet.InboundPacketQueue;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.DelaySetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;
import java.util.function.BooleanSupplier;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.login.server.S00PacketDisconnect;

public final class BlinkModule extends Module {
    public static BlinkModule INSTANCE;
    public final ModeSetting<BlinkDirection> mode;
    public final ExpandSetting resend;
    public final BooleanSetting resendEnabled;
    public final DelaySetting resendDelay;
    public final ExpandSetting autoDisable;
    public final BooleanSetting autoDisableEnabled;
    public final DelaySetting autoDisableDelay;
    public PacketHold[] holds;
    public HeldPacketQueue<?>[] queues;

    public BlinkModule() {
        super("Blink", Category.PLAYER);
        INSTANCE = this;
        mode = new ModeSetting<>(this, "Mode", BlinkDirection.OUTBOUND, BlinkDirection.values());
        mode.addListener((Runnable) this::restart);
        resend = new ExpandSetting(this, "Resend");
        resend.enabledPredicate = new ResendCondition(this);
        resend.setDescription("Send the queued packets when the time expires (like restarting Blink after the time passes).");
        resendEnabled = new BooleanSetting(resend, "Enabled", false, "Yes", "No");
        resendDelay = new DelaySetting(resend);
        autoDisable = new ExpandSetting(this, "Auto-Disable");
        autoDisable.enabledPredicate = new AutoDisableCondition(this);
        autoDisable.setDescription("Automatically disable Blink when the time expires.");
        autoDisableEnabled = new BooleanSetting(autoDisable, "Enabled", false, "Yes", "No");
        autoDisableDelay = new DelaySetting(autoDisable);
        notifyToggle = false;
        autoDisableEnabled.addListener((Runnable) () -> autoDisableDelay.resetAction.run());
        resendEnabled.addListener((Runnable) () -> resendDelay.resetAction.run());
    }

    @EventHandler public void onTick(PreClientTickEvent event) {
        if (!isEnabled()) return;
        if (resendEnabled.isEnabled() && resendDelay.hasElapsed()) restart();
        if (autoDisableEnabled.isEnabled() && autoDisableDelay.hasElapsed()) setEnabled(false, true);
    }

    @Override public void toggle(boolean enabled) {
        if (enabled && (mc.theWorld == null || mc.thePlayer == null || mc.thePlayer.sendQueue == null)) enabled = false;
        if (enabled == isEnabled()) return;
        if (enabled) {
            flags |= 2;
            try {
                acquireHolds();
                autoDisableDelay.resetAction.run();
                resendDelay.resetAction.run();
            } catch (Throwable error) {
                reportToggleError(error, enabled);
            }
        } else {
            flags &= ~2;
            releaseHolds();
        }
        if (notifyToggle) Adjust.INSTANCE.fileSystem.markDirty();
    }

    @Override public void onEnable() {
        acquireHolds();
        autoDisableDelay.resetAction.run();
        resendDelay.resetAction.run();
    }

    @Override public void onDisable() { releaseHolds(); }

    @Override public String getSuffix() { return mode.getValue().getName(); }

    public void restart() {
        if (holds != null) {
            for (int i = 0; i < holds.length; i++) queues[i].removeHold(holds[i]);
            holds = null;
        }
        if (isEnabled()) acquireHolds();
        resendDelay.resetAction.run();
    }

    public void acquireHolds() {
        if (holds != null) return;
        queues = mode.getValue().queues;
        holds = new PacketHold[queues.length];
        for (int i = 0; i < queues.length; i++) holds[i] = queues[i].addHold(null, false);
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled()) return;
        if (event.packet instanceof S00PacketDisconnect || event.packet instanceof S01PacketJoinGame) {
            if (isEnabled()) {
                flags &= ~2;
                releaseHolds();
                if (notifyToggle) Adjust.INSTANCE.fileSystem.markDirty();
            }
            Module gameSpeed = Adjust.INSTANCE.moduleManager.getModule(com.adjustclient.modules.impl.player.GameSpeedModule.class);
            if (gameSpeed != this && gameSpeed.getBind().intersects(getBind())) gameSpeed.toggle(false);
        }
        if (event.outgoing && event.packet instanceof C03PacketPlayer) {
            resendDelay.elapsedTicks++;
            autoDisableDelay.elapsedTicks++;
        }
    }

    public void releaseHolds() {
        if (holds == null) return;
        for (int i = 0; i < holds.length; i++) queues[i].removeHold(holds[i]);
        holds = null;
    }

    class AutoDisableCondition implements BooleanSupplier {
        final BlinkModule owner;
        AutoDisableCondition(BlinkModule owner) { this.owner = owner; }
        @Override public boolean getAsBoolean() { return owner.autoDisableEnabled.isEnabled(); }
    }

    static class ResendCondition implements BooleanSupplier {
        final BlinkModule owner;
        ResendCondition(BlinkModule owner) { this.owner = owner; }
        public boolean getAsBoolean() { return owner.resendEnabled.isEnabled(); }
    }
}
