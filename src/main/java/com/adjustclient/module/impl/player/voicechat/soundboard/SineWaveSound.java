package com.adjustclient.module.impl.player.voicechat.soundboard;

public class SineWaveSound implements SoundboardSound {
    public static final SineWaveSound INSTANCE = new SineWaveSound();
    public String getName() { return "Sine Wave"; }
    public short[] nextFrame() {
        short[] samples = new short[960];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = (short) (Math.sin(i / 48000.0 * 50265.48245743669) * 32767.0);
        }
        return samples;
    }
    public boolean hasRemaining() { return true; }
}
