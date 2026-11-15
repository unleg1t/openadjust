package com.adjustclient.module.impl.player.voicechat.hud;

import com.adjustclient.module.impl.player.voicechat.playback.AudioStream;
import net.minecraft.entity.player.EntityPlayer;

public final class VoiceHudEntry {
    public final EntityPlayer player;
    public final AudioStream stream;

    public VoiceHudEntry(EntityPlayer player, AudioStream stream) { this.player = player; this.stream = stream; }
}
