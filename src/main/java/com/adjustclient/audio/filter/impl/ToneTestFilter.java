package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.AudioFilter;
public final class ToneTestFilter extends AudioFilter {
    private static final FilterParameters[] TONES = {
        new FilterParameters(775, 40), new FilterParameters(581, 40),
        new FilterParameters(689, 40), new FilterParameters(775, 40)
    };
    public int sampleIndex;
    public long startTime;
    public boolean enabled;
    public String name;
    public boolean supportsTail() { return true; }
    public void process(short[] samples, int offset, int length) { startTime = System.currentTimeMillis(); }
    public void reset() { sampleIndex = 0; }
    public void processTail(short[] samples, int offset, int length) {
        long elapsed = System.currentTimeMillis() - startTime - 500;
        if (elapsed < 0) return;
        FilterParameters[] tones = TONES;
        int end = 0;
        for (int tone = 0; tone < 4; tone++) {
            int beginning = end;
            end += tones[tone].duration();
            if (elapsed > beginning && elapsed < end) {
                for (int i = 0; i < length / 2; i++) {
                    short left = samples[2 * i];
                    short right = samples[2 * i + 1];
                    sampleIndex++;
                }
            }
        }
    }
    public boolean isFinished() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed < 500) return false;
        FilterParameters[] tones = TONES;
        int duration = tones[0].duration() + tones[1].duration() + tones[2].duration() + tones[3].duration();
        return elapsed > duration + 500L;
    }
    public boolean isActive() { return false; }
}
