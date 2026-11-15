package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import java.util.UUID;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperties;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;

public class AddedUserEntry {
    public UUID userId;
    public UUID channelId;
    public UserProperties properties;

    public AddedUserEntry(UUID userId, UUID channelId, UserProperties properties) {
        this.userId = userId;
        this.channelId = channelId;
        this.properties = properties;
    }
}
