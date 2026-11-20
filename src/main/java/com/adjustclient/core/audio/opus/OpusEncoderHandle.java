package com.adjustclient.core.audio.opus;

public final class OpusEncoderHandle implements AutoCloseable {
    public OpusCodec codec;
    public synchronized byte[] encode(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            bytes[2 * i] = (byte) samples[i];
            bytes[2 * i + 1] = (byte) (samples[i] >>> 8);
        }
        return codec.encode(bytes);
    }
    public synchronized void close() { codec.close(); codec = null; }
}
