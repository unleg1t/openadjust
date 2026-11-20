package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.UserProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientboundAddUsersPacket extends ClientboundVoicePacket {
    public List<AddedUserEntry> users;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        if (!(client.listener instanceof VoiceSessionHandler handler)) return;
        for (AddedUserEntry user : users) {
            handler.controller.addUserToChannel(user.userId, user.channelId, user.properties);
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = VoicePacket.readShort(in);
        users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID userId = VoicePacket.readUUID(in);
            UUID channelId = VoicePacket.readUUID(in);
            UserProperties properties = new UserProperties();
            properties.read(in);
            users.add(new AddedUserEntry(userId, channelId, properties));
        }
    }
}
