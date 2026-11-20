package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.ConnectionState;
import java.io.ByteArrayInputStream;

public final class ClientboundEmptyPacketP extends ClientboundVoicePacket {
    @Override public void handle(VoicePacketListener listener) {
    }

    @Override public void read(ByteArrayInputStream in, int version) {}
}
