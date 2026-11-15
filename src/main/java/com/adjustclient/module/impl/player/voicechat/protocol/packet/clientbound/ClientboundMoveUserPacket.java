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

public final class ClientboundMoveUserPacket extends ClientboundVoicePacket {
    public UUID userId;
    public UUID fromChannelId;
    public UUID toChannelId;
    public UserProperties properties;

    @Override public void handle(VoicePacketListener listener) {
        if (((VoiceChatClient) listener).listener instanceof VoiceSessionHandler handler) {
            handler.controller.moveUser(userId, fromChannelId, toChannelId, properties);
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        userId = VoicePacket.readUUID(in);
        fromChannelId = VoicePacket.readUUID(in);
        toChannelId = VoicePacket.readUUID(in);
        UserProperties decoded = new UserProperties();
        decoded.read(in);
        properties = decoded;
    }
}
