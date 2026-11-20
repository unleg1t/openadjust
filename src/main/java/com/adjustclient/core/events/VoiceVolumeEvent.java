package com.adjustclient.core.events;

public class VoiceVolumeEvent extends Event {
    public final com.adjustclient.modules.impl.player.voicechat.channel.VoiceUser user;

    public VoiceVolumeEvent(com.adjustclient.modules.impl.player.voicechat.channel.VoiceUser user) {
        this.user = user;
    }
}
