package com.adjustclient.audio.filter;

public abstract class FadingAudioFilter extends AudioFilter {
    public float strength;
    public long fadeStart;
    public long fadeDuration;
    public float previousStrength;
    public boolean isActive() { return getStrength() > 0.0f; }
    public float getStrength() {
        if (fadeDuration < 1) return strength;
        long elapsed = System.currentTimeMillis() - fadeStart;
        if (elapsed >= fadeDuration) return strength;
        return ((float) (int) elapsed / (float) (int) fadeDuration) * (strength - previousStrength) + previousStrength;
    }
}
