package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import java.util.UUID;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperties;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;

public class ShownChannelEntry {
    public UUID channelId;
    public ChannelProperties properties;

    public ShownChannelEntry(UUID channelId, ChannelProperties properties) {
        this.channelId = channelId;
        this.properties = properties;
    }
}
