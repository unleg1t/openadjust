package com.adjustclient.module.impl.combat.criticals.mode;

import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.event.*;
import com.adjustclient.module.impl.combat.antibot.AntiBotModule;
import com.adjustclient.module.impl.combat.criticals.*;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.DamageUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.module.impl.player.AntiCheatModule;

public class PacketCriticalsMode extends CriticalsMode {
    public PacketCriticalsMode(CriticalsModule module) { super(module, "Packet"); }
    @EventHandler public void onAttack(AttackEntityEvent event) {
        if (!event.pre || !isEnabled()) return;
        if (AntiCheatModule.restrictionsEnabled) {
            var state = ServerTracker.INSTANCE.serverState;
            if (state != null && state.featureBlocker != null) {
                var blocker = state.featureBlocker;
                for (var antiCheat : blocker.antiCheats.values()) {
                    if (!antiCheat.isAllowed(com.adjustclient.anticheat.feature.Features.CRITICALS_PACKET)) {
                        blocker.reportBlocked(antiCheat, com.adjustclient.anticheat.feature.Features.CRITICALS_PACKET);
                        return;
                    }
                }
            }
        }
        if (module.delayTicks >= 1 || !(event.entity instanceof EntityPlayer)) return;
        EntityPlayer target = (EntityPlayer) event.entity;
        if (AntiBotModule.isBot(target.getEntityId()) || !DamageUtil.canCritical(mc.thePlayer, target)) return;
        if (BlockUtil.isColliding(mc.thePlayer.getEntityBoundingBox().offset(0.0, 0.0625, 0.0))) return;
        sendOffsets(0.0625, 0.00125);
        module.resetDelay();
    }
}
