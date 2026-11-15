package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState;
import java.io.ByteArrayInputStream;

public final class ClientboundEmptyPacketL extends ClientboundVoicePacket {
    @Override public void handle(VoicePacketListener listener) {
    }

    @Override public void read(ByteArrayInputStream in, int version) {}
}
