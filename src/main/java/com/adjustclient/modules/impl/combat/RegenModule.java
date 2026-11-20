package com.adjustclient.modules.impl.combat;

import com.adjustclient.utilities.anticheat.feature.Feature;
import com.adjustclient.core.events.*;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.Category;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.protocol.interaction.NativeInteractionHandler;

public class RegenModule extends Module {
    public static RegenModule INSTANCE;
    public final NumberSetting health;
    public final NumberSetting packets;
    public final BooleanSetting groundOnly;
    public RegenModule() {
        super("Regen", Category.COMBAT);
        INSTANCE = this;
        health = new NumberSetting(this, "Health", 15, 1, 20, 0.5);
        packets = new NumberSetting(this, "Packets", 10, 1, 100, 1);
        groundOnly = new BooleanSetting(this, "Ground Only", true);
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || event.pre) return;
        if (!com.adjustclient.utilities.anticheat.feature.Features.REGEN.isAllowed()) { setEnabled(false, true); return; }
        if (health.getFloat() <= mc.thePlayer.getHealth() || (groundOnly.isEnabled() && !mc.thePlayer.onGround)) return;
        var handler = (NativeInteractionHandler) mc.thePlayer.sendQueue.getNetworkManager().p.interactionHandler;
        for (int i = 0; i < packets.getInt(); i++) {
            var player = mc.thePlayer;
            player.sendQueue.addToSendQueue(handler.createWalkingUpdatePacket(player, player.onGround,
                    player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ,
                    player.lastReportedYaw, player.lastReportedPitch));
        }
    }
}
