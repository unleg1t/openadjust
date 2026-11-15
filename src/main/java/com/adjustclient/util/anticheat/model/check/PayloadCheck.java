package com.adjustclient.util.anticheat.model.check;
import com.adjustclient.util.anticheat.model.AntiCheatCheck;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
public final class PayloadCheck extends AntiCheatCheck {
    public Object field8;
    public boolean onPacket(Packet<?> packet) {
        if (!(packet instanceof S3FPacketCustomPayload)) return false;
        S3FPacketCustomPayload payload = (S3FPacketCustomPayload) packet;
        if (!payload.getChannelName().equals("MC|Brand")) return false;
        PacketBuffer buffer = payload.getBufferData();
        int readerIndex = buffer.readerIndex();
        // PacketBuffer supplies "VarInt too big", " > ",
        // "The received encoded string buffer length is longer than maximum allowed (",
        // "The received encoded string buffer length is less than zero! Weird string!", and
        // "The received string length is longer than maximum allowed (" validation.
        String brand = buffer.readStringFromBuffer(32767);
        buffer.readerIndex(readerIndex);
        return brand.startsWith("Hypixel ");
    }
}
