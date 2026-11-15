package com.adjustclient.module.impl.combat.criticals;

import com.adjustclient.event.Listener;
import com.adjustclient.setting.ModeOption;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.protocol.interaction.NativeInteractionHandler;

public abstract class CriticalsMode implements Listener, MinecraftInstance, ModeOption {
    public final CriticalsModule module;
    private final String name;

    protected CriticalsMode(CriticalsModule module, String name) { this.module = module; this.name = name; }

    protected void sendOffsets(double... offsets) {
        var handler = (NativeInteractionHandler) mc.thePlayer.sendQueue.getNetworkManager().p.interactionHandler;
        for (double offset : offsets) {
            var player = mc.thePlayer;
            player.sendQueue.addToSendQueue(handler.createWalkingUpdatePacket(player, false,
                    player.posX, player.posY + offset, player.posZ, player.rotationYaw, player.rotationPitch));
        }
    }
    @Override public String getName() { return name; }
    public boolean isEnabled() { return module.isEnabled() && module.mode.getValue() == this; }
}
