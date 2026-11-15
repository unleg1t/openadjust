package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.AudioFilter;

public final class NoiseFilter extends AudioFilter {
    public float amplitude;
    public boolean enabled;
    public boolean pending;
    public String name;
    public boolean supportsTail() { return true; }
    public void process(short[] samples, int offset, int length) { pending = true; }
    public void processTail(short[] samples, int offset, int length) {
        for (int i = 0; i < samples.length / 2; i++) {
            double noise = (Math.random() * 2.0 - 1.0) * amplitude;
            for (int channel = 0; channel < 2; channel++) {
                int index = 2 * i + channel;
                double value = samples[index] + noise;
                samples[index] = value < -32768.0 ? Short.MIN_VALUE : value >= 32767.0 ? Short.MAX_VALUE : (short) (int) value;
            }
        }
        pending = false;
    }
    public boolean isFinished() { return !pending; }
    public boolean isActive() { return false; }
}
