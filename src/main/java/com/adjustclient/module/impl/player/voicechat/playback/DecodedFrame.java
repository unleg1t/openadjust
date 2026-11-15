package com.adjustclient.module.impl.player.voicechat.playback;

public final class DecodedFrame {
    public final short[] samples;
    public final long sequence;
    public int readOffset;

    public DecodedFrame(short[] samples, long sequence) {
        this.samples = samples;
        this.sequence = sequence;
    }
}
