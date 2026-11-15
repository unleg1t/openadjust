package com.adjustclient.module.impl.player.voicechat.soundboard;

public interface SoundboardSound {
    String getName();
    short[] nextFrame();
    boolean hasRemaining();
}
