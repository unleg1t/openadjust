package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.module.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientboundUserStatePacket extends ClientboundVoicePacket {
    public final List<UUID> userIds = new ArrayList<>();

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        for (UUID userId : userIds) {
            if (client.listener instanceof VoiceSessionHandler handler) handler.users.get(userId);
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = (byte) in.read();
        for (int i = 0; i < count; i++) userIds.add(VoicePacket.readUUID(in));
    }
}
