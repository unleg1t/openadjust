package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.ConnectionState;
import java.io.ByteArrayInputStream;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumB;

public final class ClientboundInvalidEncryptionPacket extends ClientboundVoicePacket {
    public ClientboundInvalidEncryptionPacket() {
        phase = com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumB.HANDSHAKE;
        encryption = com.adjustclient.modules.impl.player.voicechat.protocol.type.PacketEncryption.NONE;
    }

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        client.udpClient.setState(ConnectionState.AUTHENTICATION_FAILED);
        client.listener.onDisconnect(ConnectionState.AUTHENTICATION_FAILED, "Invalid encryption");
    }

    @Override public void read(ByteArrayInputStream in, int version) {}
}
