package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ClientboundStringListPacket extends ClientboundVoicePacket {
    public String value;
    public List<String> values;

    @Override public void handle(VoicePacketListener listener) {
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        value = VoicePacket.readString(StandardCharsets.UTF_8, in);
        if (value.isEmpty()) value = null;
        int count = VoicePacket.readShort(in);
        List<String> decoded = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String element = VoicePacket.readString(StandardCharsets.UTF_8, in);
            decoded.add(element.isEmpty() ? null : element);
        }
        values = decoded;
    }
}
