package com.adjustclient.modules.impl.player.voicechat.soundboard;

public final class SquareWaveSound implements SoundboardSound {
    public static final SquareWaveSound INSTANCE = new SquareWaveSound();

    @Override
    public String getName() {
        return "Square Wave";
    }

    @Override
    public short[] nextFrame() {
        short[] samples = new short[960];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = i % 10 < 5 ? Short.MAX_VALUE : Short.MIN_VALUE;
        }
        return samples;
    }

    @Override
    public boolean hasRemaining() {
        return true;
    }
}
