package com.adjustclient.core.events;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

public class ClientBrandEvent extends CancellableEvent {
    public String brand;
    public List<C17PacketCustomPayload> packets = new ArrayList<>();

    public ClientBrandEvent(String brand) {
        this.brand = brand;
    }

    public void buildPacket(String channel, String payload) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(payload.getBytes(StandardCharsets.UTF_8));
        if (buffer.writerIndex() >= 32768) {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
        packets.add(new C17PacketCustomPayload(channel, new PacketBuffer(buffer)));
    }
}
