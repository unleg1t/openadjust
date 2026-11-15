package com.adjustclient.audio;

public final class Denoiser implements AutoCloseable {
    public de.maxhenkel.rnnoise4j.Denoiser denoiser;
    public synchronized void denoise(short[] samples) {
        if (denoiser != null) {
            short[] result = denoiser.denoise(samples);
            System.arraycopy(result, 0, samples, 0, samples.length);
        }
    }
    public synchronized void close() {
        if (denoiser != null) { denoiser.close(); denoiser = null; }
    }
}
