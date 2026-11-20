package com.adjustclient.modules.impl.player.voicechat.soundboard;

public interface SoundboardSound {
    String getName();
    short[] nextFrame();
    boolean hasRemaining();
}
