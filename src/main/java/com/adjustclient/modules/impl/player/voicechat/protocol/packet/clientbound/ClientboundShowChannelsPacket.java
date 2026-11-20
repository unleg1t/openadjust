package com.adjustclient.modules.impl.player.voicechat.protocol.packet.clientbound;

import com.adjustclient.modules.impl.player.voicechat.channel.VoiceChatController;
import com.adjustclient.modules.impl.player.voicechat.channel.model.Channel;
import com.adjustclient.modules.impl.player.voicechat.channel.model.DefaultChannel;
import com.adjustclient.modules.impl.player.voicechat.channel.model.NamedChannel;
import com.adjustclient.modules.impl.player.voicechat.client.VoiceChatClient;
import com.adjustclient.modules.impl.player.voicechat.client.session.VoiceSessionHandler;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.modules.impl.player.voicechat.protocol.property.ChannelProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ClientboundShowChannelsPacket extends ClientboundVoicePacket {
    public List<ShownChannelEntry> channels;

    @Override public void handle(VoicePacketListener listener) {
        VoiceChatClient client = (VoiceChatClient) listener;
        for (ShownChannelEntry entry : channels) {
            if (!(client.listener instanceof VoiceSessionHandler handler)) continue;
            VoiceChatController controller = handler.controller;
            try {
                Channel channel = entry.channelId.getMostSignificantBits() == 0L
                        && entry.channelId.getLeastSignificantBits() == 0L
                        ? new DefaultChannel() : new NamedChannel(entry.channelId);
                channel.properties = entry.properties;
                controller.getChannelRegistry().addChannel(channel);
            } catch (RuntimeException exception) {
                controller.reportError(true, "Can't show channel with id " + entry.channelId
                        + " because of an error: " + exception.getMessage());
            }
        }
    }

    @Override public void read(ByteArrayInputStream in, int version) throws IOException {
        int count = VoicePacket.readShort(in);
        channels = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID channelId = VoicePacket.readUUID(in);
            ChannelProperties properties = new ChannelProperties();
            properties.read(in);
            channels.add(new ShownChannelEntry(channelId, properties));
        }
    }
}
