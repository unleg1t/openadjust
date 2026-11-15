package com.adjustclient.event;

public class VoiceStreamRemovedEvent extends VoiceAudioEvent {
    public final com.adjustclient.module.impl.player.voicechat.playback.PlayerAudioStream stream;

    public VoiceStreamRemovedEvent(com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo user,
            com.adjustclient.module.impl.player.voicechat.playback.PlayerAudioStream stream) {
        this.field4 = user;
        this.stream = stream;
    }
}
