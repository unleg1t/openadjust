package com.adjustclient.core.events;

public class VoiceStreamRemovedEvent extends VoiceAudioEvent {
    public final com.adjustclient.modules.impl.player.voicechat.playback.PlayerAudioStream stream;

    public VoiceStreamRemovedEvent(com.adjustclient.modules.impl.player.voicechat.client.user.VoiceUserInfo user,
            com.adjustclient.modules.impl.player.voicechat.playback.PlayerAudioStream stream) {
        this.field4 = user;
        this.stream = stream;
    }
}
