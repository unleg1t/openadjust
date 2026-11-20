package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.UserProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH;

public final class ClientboundAudioPacket extends ClientboundVoicePacket {
    public ClientboundAudioPacket() {
        reliability = com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumH.UDP;
    }

    public UUID senderId;
    public byte[] data;

    @Override public void handle(VoicePacketListener listener) {
        ((VoiceChatClient) listener).listener.onAudio(senderId, data);
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        senderId = VoicePacket.readUUID(in);
        int length = VoicePacket.readInt(in);
        if (length > 1024) throw new IllegalStateException("Packet too large");
        data = new byte[length];
        in.read(data);
    }
}
