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

public final class ClientboundDisconnectPacket extends ClientboundVoicePacket {
    public String reason;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        client.udpClient.setState(ConnectionState.KICK);
        client.listener.onDisconnect(ConnectionState.KICK, reason);
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        reason = VoicePacket.readString(java.nio.charset.StandardCharsets.UTF_8, in);
        if (reason.isEmpty()) reason = null;
    }
}
