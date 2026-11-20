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

public final class ClientboundRemoveUsersPacket extends ClientboundVoicePacket {
    public List<RemovedUserEntry> users;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        for (RemovedUserEntry user : users) {
            if (client.listener instanceof VoiceSessionHandler handler) {
                handler.controller.removeUserFromChannel(user.userId, user.channelId);
            }
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = VoicePacket.readShort(in);
        users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            users.add(new RemovedUserEntry(VoicePacket.readUUID(in), VoicePacket.readUUID(in)));
        }
    }
}
