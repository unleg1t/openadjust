package com.adjustclient.module.impl.combat.velocity;

import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.combat.velocity.mode.*;
import com.adjustclient.setting.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S27PacketExplosion;

public final class VelocityModule extends Module implements MinecraftInstance {
    public static VelocityModule INSTANCE;
    public final ModeSetting<VelocityMode> mode;
    public final BooleanSetting allowSelfVelocity;
    public final Timer selfVelocityTimer = new Timer(0L);
    public final Timer explosionTimer = new Timer(0L);
    public boolean explosion;

    public VelocityModule() {
        super("Velocity", Category.COMBAT);
        INSTANCE = this;
        NormalVelocityMode normal = new NormalVelocityMode(this);
        ZeroVelocityMode zero = new ZeroVelocityMode(this);
        AirVelocityMode air = new AirVelocityMode(this);
        mode = new ModeSetting<>(this, "Mode", normal, normal, zero, air);
        getSettings().remove(mode);
        getSettings().add(0, mode);
        mode.addListener((oldValue, newValue) -> {
            if (!isEnabled() || oldValue == newValue) return;
            if (oldValue instanceof VelocityMode) ((VelocityMode) oldValue).reset();
            if (newValue instanceof VelocityMode) ((VelocityMode) newValue).reset();
        });
        allowSelfVelocity = new BooleanSetting(this, "Allow self velocity", true);
        allowSelfVelocity.setDescription("Allows velocity from used items such as wind charges.");
    }
    @EventHandler public void onItemUse(ItemUseNotificationEvent event) {
        if (!isEnabled()) return;
        ItemStack stack = event.stack;
        if (stack != null && stack.getItem() instanceof ItemFireball) selfVelocityTimer.reset();
    }
    @Override public void onEnable() {
        selfVelocityTimer.lastMs = 0;
        explosionTimer.lastMs = 0;
    }
    @Override public void onDisable() {
        if (mode.getValue() != null) mode.getValue().reset();
        explosion = false;
    }
    @Override public String getSuffix() {
        VelocityMode selected = mode.getValue();
        return selected.getSuffix();
    }
    public boolean isSelfVelocity() {
        if (!allowSelfVelocity.isEnabled()) return false;
        if (allowSelfVelocity.isHidden()) return false;
        var blocker = ServerTracker.INSTANCE.serverState;
        int delay = blocker == null || blocker.packetWatcher.delayCount == 0 ? 400
                : Math.max(50, blocker.packetWatcher.totalDelay / blocker.packetWatcher.delayCount) + 350;
        return selfVelocityTimer.lastMs + delay > System.currentTimeMillis();
    }
    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null || event.outgoing || !(event.packet instanceof S27PacketExplosion)) return;
        S27PacketExplosion packet = (S27PacketExplosion) event.packet;
        var blocker = ServerTracker.INSTANCE.serverState;
        if (blocker != null && blocker.antiCheatModel != null
                && blocker.antiCheatModel.serverType == ServerType.HYPIXEL
                && (packet.func_149149_c() != 0 || packet.func_149144_d() != 0 || packet.func_149147_e() != 0)) explosion = true;
    }
}
