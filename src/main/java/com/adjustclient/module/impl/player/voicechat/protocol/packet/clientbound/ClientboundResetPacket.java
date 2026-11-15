package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState;
import java.io.ByteArrayInputStream;

public final class ClientboundResetPacket extends ClientboundVoicePacket {
    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        if (client.listener instanceof com.adjustclient.module.impl.player.voicechat.client.session.VoiceSessionHandler handler) {
            handler.controller.reset();
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) {}
}
