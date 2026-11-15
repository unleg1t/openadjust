package com.adjustclient.protocol.interaction;
import net.minecraft.client.entity.EntityPlayerSP;
import com.adjustclient.protocol.type.SwingReason;
import com.adjustclient.Adjust;
public interface InteractionHandler {
    default void swingAndRun(EntityPlayerSP player, SwingReason reason, Runnable action) {
        if (com.adjustclient.Adjust.mouseClickHelper != null
                && com.adjustclient.Adjust.mouseClickHelper.left != null
                && com.adjustclient.Adjust.mouseClickHelper.left.key != null
                && !com.adjustclient.Adjust.mouseClickHelper.left.swing
                && com.adjustclient.Adjust.mouseClickHelper.left.key.pressTime < 1) {
            player.sendQueue.getNetworkManager().sendPacket(new net.minecraft.network.play.client.C0APacketAnimation());
        } else {
            player.swingItem();
        }
        action.run();
    }
}
