package com.adjustclient.core.audio.filter.impl;

import com.adjustclient.core.audio.filter.AudioFilter;

public final class CompressorFilter extends AudioFilter {
    public float threshold;
    public double ratio;
    public boolean enabled;
    public String name;
    public void process(short[] samples, int offset, int length) {
        for (int i = 0; i < samples.length; i++) {
            double value = (double) samples[i] / 32768.0;
            double magnitude = Math.abs(value);
            if (magnitude > threshold) value = Math.signum(value) * ((magnitude - threshold) / ratio + threshold);
            samples[i] = (short) (int) (value * 32768.0);
        }
    }
    public boolean isFinished() { return true; }
    public boolean isActive() { return false; }
}
