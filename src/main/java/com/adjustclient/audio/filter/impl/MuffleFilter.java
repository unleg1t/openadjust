package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.FadingAudioFilter;

public final class MuffleFilter extends FadingAudioFilter {
    public short previousSample;
    public boolean initialized;
    public String name;
    public void process(short[] samples, int offset, int length) {
        if (!initialized) { previousSample = 0; initialized = true; }
        double mix = getStrength();
        for (int i = 0; i < length; i++) {
            short amplified = (short) (3 * samples[i]);
            samples[i] = (short) (int) (amplified * (1.0 - mix) + (short) (amplified - previousSample) * mix);
            previousSample = amplified;
        }
    }
    public void reset() { initialized = false; previousSample = 0; }
    public boolean isFinished() { return true; }
}
