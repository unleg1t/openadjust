package com.adjustclient.core.events;

public class VoicePlayerLeaveEvent extends Event {
    public final java.util.UUID userId;
    public final com.adjustclient.modules.impl.player.voicechat.channel.model.Channel channel;

    public VoicePlayerLeaveEvent(java.util.UUID userId,
            com.adjustclient.modules.impl.player.voicechat.channel.model.Channel channel) {
        this.userId = userId;
        this.channel = channel;
    }
}
