package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.AudioFilter;

public final class AutomaticGainFilter extends AudioFilter {
    public boolean enabled;
    public double targetLevel;
    public double levelSum;
    public double sampleCount;
    public String name;
    public void postProcess(short[] samples, int offset, int length) {
        double level = com.adjustclient.audio.AudioUtil.level(samples, 0, length) + 30.0;
        level = net.minecraft.util.MathHelper.clamp_double(level, -127.0, 32.0);
        double exponent = level > 1.0 ? level / 20.0 : level / 10.0;
        double gain = net.minecraft.util.MathHelper.clamp_double(targetLevel / Math.pow(10.0, exponent), 0.0, 1.0);
        for (int i = 0; i < length; i++) {
            levelSum += gain;
            sampleCount += 1.0;
            samples[i] = (short) (int) (samples[i] * (levelSum / (sampleCount + 1.0)));
            if (gain == 1.0) { levelSum *= 0.998; sampleCount *= 0.998; }
        }
    }
    public void reset() { levelSum = 0.0; sampleCount = 0.0; }
    public boolean isFinished() { return true; }
    public boolean isActive() { return enabled; }
}
