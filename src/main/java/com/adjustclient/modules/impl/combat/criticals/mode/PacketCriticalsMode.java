package com.adjustclient.modules.impl.combat.criticals.mode;

import com.adjustclient.utilities.anticheat.feature.ModuleFeature;
import com.adjustclient.core.events.*;
import com.adjustclient.modules.impl.combat.antibot.AntiBotModule;
import com.adjustclient.modules.impl.combat.criticals.*;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.DamageUtil;
import com.adjustclient.utilities.anticheat.ServerTracker;
import net.minecraft.entity.player.EntityPlayer;
import com.adjustclient.modules.impl.player.AntiCheatModule;

public class PacketCriticalsMode extends CriticalsMode {
    public PacketCriticalsMode(CriticalsModule module) { super(module, "Packet"); }
    @EventHandler public void onAttack(AttackEntityEvent event) {
        if (!event.pre || !isEnabled()) return;
        if (AntiCheatModule.restrictionsEnabled) {
            var state = ServerTracker.INSTANCE.serverState;
            if (state != null && state.featureBlocker != null) {
                var blocker = state.featureBlocker;
                for (var antiCheat : blocker.antiCheats.values()) {
                    if (!antiCheat.isAllowed(com.adjustclient.utilities.anticheat.feature.Features.CRITICALS_PACKET)) {
                        blocker.reportBlocked(antiCheat, com.adjustclient.utilities.anticheat.feature.Features.CRITICALS_PACKET);
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
