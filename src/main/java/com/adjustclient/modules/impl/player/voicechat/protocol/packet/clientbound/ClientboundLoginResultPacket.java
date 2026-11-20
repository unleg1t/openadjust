package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceLoginStatus;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumB;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class ClientboundLoginResultPacket extends ClientboundVoicePacket {
    public ClientboundLoginResultPacket() {
        phase = com.adjustclient.modules.impl.player.voicechat.protocol.type.VoiceProtocolEnumB.HANDSHAKE;
    }

    public VoiceLoginStatus status;
    public int value;
    public boolean flag;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        if (status == VoiceLoginStatus.SUCCESS) {
            client.udpClient.phase = VoiceProtocolEnumB.CONNECTED;
            client.listener.onAuthenticated(flag);
            return;
        }
        switch (status) {
            case AUTHENTICATION_FAILED -> client.udpClient.setState(ConnectionState.AUTHENTICATION_FAILED);
            case ALREADY_CONNECTED -> client.udpClient.setState(ConnectionState.ALREADY_CONNECTED);
            default -> client.udpClient.setState(ConnectionState.DISCONNECT);
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        value = com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket.readInt(in);
        int code = in.read();
        if (code == -1) status = null;
        else {
            if (code >= 3) throw new IllegalStateException("No Enum found");
            status = VoiceLoginStatus.values()[code];
        }
        int flagValue = in.read();
        flag = flagValue != -1 && flagValue != 0;
    }
}
