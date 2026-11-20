package com.adjustclient.core.audio.filter.impl;

import com.adjustclient.core.audio.filter.FadingAudioFilter;

public final class EchoFilter extends FadingAudioFilter {
    public short[] delayBuffer;
    public int echoCount;
    public int delaySamples;
    public int writeIndex;
    public int silentSamples;
    public boolean inputWritten;
    public String name;
    public boolean supportsTail() { return true; }
    public void process(short[] samples, int offset, int length) {
        if (delayBuffer == null) delayBuffer = new short[echoCount * delaySamples];
        inputWritten = true;
        if (length > 0) silentSamples = 0;
        for (int i = 0; i < length; i++) {
            delayBuffer[writeIndex] = samples[i];
            writeIndex = (writeIndex + 1) % delayBuffer.length;
        }
    }
    public void reset() { delayBuffer = null; writeIndex = 0; silentSamples = 0; }
    public void processTail(short[] samples, int offset, int length) {
        if (delayBuffer == null) return;
        if (!inputWritten) {
            for (int i = 0; i < length / 2; i++) {
                silentSamples++;
                delayBuffer[writeIndex] = 0;
                writeIndex = (writeIndex + 1) % delayBuffer.length;
            }
        }
        for (int i = 0; i < length; i++) {
            short value = samples[i];
            int position = (delayBuffer.length + (i >>> 1) + writeIndex + length) % delayBuffer.length;
            for (int echo = 1; echo <= echoCount; echo++) {
                int index = (delayBuffer.length + position - delaySamples * echo) % delayBuffer.length;
                float delayed = ((float) delayBuffer[index] / ((float) (echo * echo) / 10.0F + 1.0F)) / 30.0F;
                if (echo % 2 == (i & 1)) delayed /= 2.0F;
                value = (short) (value + (short) (int) (delayed * getStrength()));
            }
            samples[i] = value;
        }
        inputWritten = false;
    }
    public boolean isFinished() { return !isActive() || delayBuffer == null || silentSamples > delayBuffer.length; }
    public boolean isActive() { return super.isActive() && echoCount >= 1 && delaySamples >= 1; }
}
