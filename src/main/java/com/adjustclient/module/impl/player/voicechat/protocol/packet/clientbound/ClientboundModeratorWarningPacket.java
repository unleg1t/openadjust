package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientboundModeratorWarningPacket extends ClientboundVoicePacket {
    public String message;

    @Override public void handle(VoicePacketListener listener) {
        ((VoiceChatClient) listener).listener.onModeratorWarning(message);
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        message = VoicePacket.readString(java.nio.charset.StandardCharsets.UTF_8, in);
        if (message.isEmpty()) message = null;
    }
}
