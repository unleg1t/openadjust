package com.adjustclient.module.impl.player.voicechat.protocol.packet.clientbound;

import java.util.UUID;
import com.adjustclient.module.impl.player.voicechat.protocol.property.ChannelProperties;
import com.adjustclient.module.impl.player.voicechat.protocol.property.UserProperties;

public class RemovedUserEntry {
    public UUID userId;
    public UUID channelId;

    public RemovedUserEntry(UUID userId, UUID channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }
}
