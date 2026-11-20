package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServerboundDisconnectPacket extends ServerboundVoicePacket {
    public final String reason;
    public ServerboundDisconnectPacket(String reason) { this.reason = reason; }
    @Override public void write(ByteArrayOutputStream out, int version) throws IOException { writeString(reason, StandardCharsets.UTF_8, out); }
}
