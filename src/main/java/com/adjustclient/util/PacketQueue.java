package com.adjustclient.util;

import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

public final class PacketQueue extends LinkedBlockingQueue<Packet<?>> {
    public boolean flushing;
    public Object field40;
    @Override public boolean add(Packet<?> packet) {
        if (flushing) return false;
        return super.add(packet); // preserves the inherited "Queue full" exception
    }
    public void flush() {
        if (flushing || size() < 1) return;
        flushing = true;
        Minecraft mc = Minecraft.getMinecraft();
        while (size() > 0) {
            Packet<?> packet = poll();
            if (mc.thePlayer != null) mc.thePlayer.sendQueue.getNetworkManager().sendPacket(packet);
        }
        flushing = false;
    }
}
