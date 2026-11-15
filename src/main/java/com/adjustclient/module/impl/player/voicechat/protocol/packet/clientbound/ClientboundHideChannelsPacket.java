package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.module.impl.player.voicechat.channel.VoiceChatController;
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

public final class ClientboundHideChannelsPacket extends ClientboundVoicePacket {
    public List<UUID> channelIds;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        if (!(client.listener instanceof VoiceSessionHandler handler)) return;
        VoiceChatController controller = handler.controller;
        for (UUID channelId : channelIds) {
            try {
                controller.getChannelRegistry().getChannel(channelId);
            } catch (RuntimeException exception) {
                controller.reportError(true, "Can't hide channel with id " + channelId
                        + " because of an error: " + exception.getMessage());
            }
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = VoicePacket.readShort(in);
        channelIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) channelIds.add(VoicePacket.readUUID(in));
    }
}
