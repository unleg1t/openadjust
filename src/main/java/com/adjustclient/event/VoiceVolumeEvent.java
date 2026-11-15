package com.adjustclient.event;

public class VoiceVolumeEvent extends Event {
    public final com.adjustclient.module.impl.player.voicechat.channel.VoiceUser user;

    public VoiceVolumeEvent(com.adjustclient.module.impl.player.voicechat.channel.VoiceUser user) {
        this.user = user;
    }
}
