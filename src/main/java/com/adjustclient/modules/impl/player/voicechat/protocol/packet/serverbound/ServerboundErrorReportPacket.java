package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServerboundErrorReportPacket extends ServerboundVoicePacket {
    public final String message;
    public final boolean critical;
    public ServerboundErrorReportPacket(String message, boolean critical) { this.message = message; this.critical = critical; }
    @Override public void write(ByteArrayOutputStream out, int version) throws IOException {
        writeString(message, StandardCharsets.UTF_8, out);
        out.write(critical ? 1 : 0);
    }
}
