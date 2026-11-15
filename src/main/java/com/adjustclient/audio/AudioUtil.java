package com.adjustclient.audio;

public final class AudioUtil {
    public static void swap(short[] first, short[] second, int offset, int length) {
        for (int i = 0; i < first.length; i++) {
            short temporary = second[i]; second[i] = first[i]; first[i] = temporary;
        }
    }
    public static double level(short[] samples, int offset, int length) {
        if (length < 1) return -127.0;
        double sum = 0.0;
        for (int i = offset; i < length; i++) {
            double value = (float) samples[i] / 32768.0f;
            sum += value * value;
        }
        double rms = Math.sqrt(sum / length);
        if (rms <= 0.0) return -127.0;
        return Math.min(Math.max(20.0 * Math.log10(rms), -127.0), 0.0);
    }
}
