package com.adjustclient.modules.impl.player.voicechat.playback;

import com.adjustclient.modules.impl.player.voicechat.client.user.VoiceUserInfo;

public class PlayerAudioStream extends AudioStream {
    public final VoiceUserInfo userInfo;

    public PlayerAudioStream(VoiceUserInfo userInfo) {
        this.userInfo = userInfo;
        this.playerId = userInfo.userId;
    }

}
