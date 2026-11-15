package com.adjustclient.event;

public class VoicePlayerLeaveEvent extends Event {
    public final java.util.UUID userId;
    public final com.adjustclient.module.impl.player.voicechat.channel.model.Channel channel;

    public VoicePlayerLeaveEvent(java.util.UUID userId,
            com.adjustclient.module.impl.player.voicechat.channel.model.Channel channel) {
        this.userId = userId;
        this.channel = channel;
    }
}
