package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.modules.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.ChannelProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

public final class ClientboundUpdateChannelPacket extends ClientboundVoicePacket {
    public UUID channelId;
    public ChannelProperties properties;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        if (!(client.listener instanceof VoiceSessionHandler handler)) return;
        VoiceChatController controller = handler.controller;
        Channel channel = controller.getChannelRegistry().channels.get(channelId);
        if (channel == null) {
            controller.reportError(true, "Can't update channel with id " + channelId + " because it doesn't exist");
            return;
        }
        channel.properties = properties;
        channel.getUsers();
        boolean proximity = controller.isCurrentChannelProximity();
        if (proximity != controller.currentChannelProximity) {
            controller.currentChannelProximity = proximity;
            com.adjustclient.modules.impl.player.voicechat.VoiceChatModule.INSTANCE
                    .getAudioStreamManager().clear();
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        channelId = VoicePacket.readUUID(in);
        ChannelProperties decoded = new ChannelProperties();
        decoded.read(in);
        properties = decoded;
    }
}
