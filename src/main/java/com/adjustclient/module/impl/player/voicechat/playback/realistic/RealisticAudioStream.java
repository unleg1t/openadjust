package com.adjustclient.module.impl.player.voicechat.playback.realistic;

import com.adjustclient.module.impl.player.voicechat.client.user.VoiceUserInfo;
import com.adjustclient.module.impl.player.voicechat.playback.PlayerAudioStream;
import com.adjustclient.module.impl.player.voicechat.playback.VoiceFilterChain;
import net.minecraft.entity.player.EntityPlayer;

public class RealisticAudioStream extends PlayerAudioStream {
    public final EntityPlayer entity;

    public RealisticAudioStream(EntityPlayer entity, VoiceUserInfo userInfo) {
        super(userInfo);
        this.entity = entity;
    }

    @Override
    public boolean isTalking() { return lastPacketTime + 500L > System.currentTimeMillis(); }

    @Override
    public boolean isExpired() { return super.isExpired() || entity.isDead; }
}
