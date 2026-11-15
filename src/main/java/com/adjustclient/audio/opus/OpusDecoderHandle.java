package com.adjustclient.audio.opus;

public final class OpusDecoderHandle implements AutoCloseable {
    public OpusCodec codec;
    public synchronized void close() { codec.close(); codec = null; }
}
