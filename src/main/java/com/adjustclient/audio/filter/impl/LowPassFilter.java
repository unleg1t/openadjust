package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.FadingAudioFilter;

public final class LowPassFilter extends FadingAudioFilter {
    public short previousSample;
    public boolean initialized;
    public double coefficient;
    public String name;
    public void process(short[] samples, int offset, int length) {
        if (!initialized) { previousSample = 0; initialized = true; }
        double mix = getStrength() * net.minecraft.util.MathHelper.clamp_double(coefficient - 1.0, 0.0, 1.0);
        double divisor = coefficient < 2.0 ? 2.0 : coefficient;
        for (int i = 0; i < length; i++) {
            short sample = samples[i];
            previousSample = (short) (previousSample + (short) (int) ((sample - previousSample) / divisor));
            samples[i] = (short) (int) (sample * (1.0 - mix) + previousSample * mix);
        }
    }
    public void reset() { initialized = false; previousSample = 0; }
    public boolean isFinished() { return true; }
    public boolean isActive() { return super.isActive() && coefficient > 0.0; }
}
