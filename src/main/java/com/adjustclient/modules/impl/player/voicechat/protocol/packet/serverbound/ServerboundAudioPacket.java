package com.adjustclient.modules.impl.player.voicechat.protocol.packet.serverbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ServerboundVoicePacket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH;

public final class ServerboundAudioPacket extends ServerboundVoicePacket {
    public final byte[] data;
    public ServerboundAudioPacket(byte[] data) {
        reliability = com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH.UDP;
        this.data = data;
    }
    @Override public void write(ByteArrayOutputStream out, int version) throws IOException {
        writeInt(data.length, out);
        out.write(data);
    }
}
