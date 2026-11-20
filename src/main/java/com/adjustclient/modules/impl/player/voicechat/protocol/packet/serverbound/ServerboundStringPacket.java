package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ServerboundStringPacket extends ServerboundVoicePacket {
    public Enum<?> mode;
    public byte[] data;
    public UUID field24;
    public String value;

    @Override public void write(ByteArrayOutputStream out, int version) throws IOException {
        writeInt(5, out);
        out.write(data);
        writeUUID(null, out);
        out.write(mode == null ? -1 : mode.ordinal());
        writeString(value, StandardCharsets.UTF_8, out);
    }

    @Override public byte[] getSupportedVersions() {
        return new byte[] {0, (byte) 128, 26, (byte) 177, (byte) 177, 31, (byte) 220, 78, (byte) 128};
    }
}
