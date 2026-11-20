package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;

public final class UnusedServerboundPacketL extends ServerboundVoicePacket {
    @Override public void write(ByteArrayOutputStream out, int version) { }
}
