package com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;

public final class UnusedServerboundPacketG extends ServerboundVoicePacket {
    @Override public void write(ByteArrayOutputStream out, int version) { }
}
