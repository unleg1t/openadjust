package com.adjustclient.module.impl.combat.velocity.mode;

import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IncomingVelocityEvent;
import com.adjustclient.module.impl.combat.velocity.*;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import com.adjustclient.module.impl.player.AntiCheatModule;

public final class ZeroVelocityMode extends VelocityMode {
    public static ZeroVelocityMode INSTANCE;
    public ZeroVelocityMode(VelocityModule module) { super(module, "Zero"); INSTANCE = this; }
    @EventHandler public void onPacket(IncomingVelocityEvent event) {
        if (event.isCancelled() || !isActive() || mc.thePlayer == null || module.isSelfVelocity()) return;
        if (event.packet instanceof S12PacketEntityVelocity
                && ((S12PacketEntityVelocity) event.packet).getEntityID() != mc.thePlayer.getEntityId()) return;
        var blocker = ServerTracker.INSTANCE.serverState;
        if (AntiCheatModule.restrictionsEnabled && blocker != null && blocker.featureBlocker != null) {
            var features = blocker.featureBlocker;
            ModuleFeature feature = com.adjustclient.anticheat.feature.Features.VELOCITY_VERTICAL;
            for (var antiCheat : features.antiCheats.values()) {
                if (!antiCheat.isAllowed(feature)) {
                    features.reportBlocked(antiCheat, feature);
                    return;
                }
            }
        }
        event.cancelled = true;
        event.suppressApplication = true;
    }
}
