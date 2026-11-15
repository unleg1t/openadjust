package com.adjustclient.game.packet;

import com.adjustclient.Adjust;
import io.netty.channel.EventLoop;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.Packet;

public final class InboundPacketQueue extends HeldPacketQueue<INetHandlerPlayClient> {
    public static EventLoop fallbackEventLoop;
    public void receivePacket(PacketHold hold) {
        EventLoop loop = getNetworkManager();
        if (loop == null) return;
        if (!loop.inEventLoop()) {
            loop.execute(() -> super.removeHold(hold));
            return;
        }
        super.removeHold(hold);
    }

    @Override
    public void removeHold(PacketHold hold) { receivePacket(hold); }

    public boolean shouldDelay(Packet<INetHandlerPlayClient> packet) {
        return Adjust.entityTrackerService.filter.shouldDelay(packet) && super.isPacketAllowed(packet);
    }

    @Override
    public boolean isPacketAllowed(Packet<?> packet) {
        return shouldDelay((Packet<INetHandlerPlayClient>) packet);
    }

    public static EventLoop getNetworkManager() {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            if (mc.thePlayer.sendQueue != null) {
                return mc.thePlayer.sendQueue.getNetworkManager().channel.eventLoop();
            }
        } catch (NullPointerException ignored) {
        }
        return fallbackEventLoop;
    }

    public void processNow(Runnable action) {
        EventLoop loop = getNetworkManager();
        if (loop == null) return;
        if (loop.inEventLoop()) action.run();
        else loop.execute(action);
    }
}
