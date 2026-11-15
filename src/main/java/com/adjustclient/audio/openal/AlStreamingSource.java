package com.adjustclient.audio.openal;

import java.util.Arrays;
import org.lwjgl.openal.AL10;
import org.joml.Vector3f;
import com.adjustclient.audio.device.ChannelMode;

public final class AlStreamingSource {
    public ChannelMode channels;
    public AlSource source;
    public AlBufferHandle buffers;
    public int sampleRate;
    public int bufferSize;
    public int prefillCount;
    public int nextBuffer;
    public boolean submitted;
    public Object field36;
    public AlStreamingSource(ChannelMode channels, AlSource source, AlBufferHandle buffers, int sampleRate, int bufferSize, int prefillCount) {
        this.channels = channels; this.source = source; this.buffers = buffers;
        this.sampleRate = sampleRate; this.bufferSize = bufferSize; this.prefillCount = prefillCount;
    }
    public void unqueueProcessed() {
        int count = AL10.alGetSourcei(source.id, 4118);
        OpenAlErrors.checkAlError();
        for (int i = 0; i < count; i++) source.unqueueBuffer();
    }
    public void setListenerOrientation(Vector3f direction) {
        AL10.alListenerfv(4111, new float[] {direction.x, direction.y, direction.z, 0.0f, 1.0f, 0.0f});
        OpenAlErrors.checkAlError();
    }
    public void submit(ChannelMode channels, short[] samples, int offset, int length) {
        if (this.channels != channels) throw new IllegalArgumentException("Channel type mismatch: got " + channels.name() + " but expected " + this.channels.name());
        unqueueProcessed();
        AlSourceState state = source.getState();
        int queued = AL10.alGetSourcei(source.id, 4117);
        OpenAlErrors.checkAlError();
        if (state != AlSourceState.INITIAL && state != AlSourceState.PAUSED && state != AlSourceState.STOPPED && queued >= 1) {
            queue(channels, samples, 0, length);
            submitted = true;
        } else {
            prefill();
            queue(channels, samples, 0, length);
            submitted = true;
            source.play();
        }
    }
    public void prefill() {
        for (int i = 0; i < prefillCount; i++) queue(channels, new short[bufferSize], 0, bufferSize);
    }
    public void queue(ChannelMode channels, short[] samples, int offset, int length) {
        int queued = AL10.alGetSourcei(source.id, 4117);
        OpenAlErrors.checkAlError();
        if (queued >= buffers.ids.length) {
            source.setSampleOffset(source.getSampleOffset() + bufferSize * (queued - prefillCount));
            unqueueProcessed();
        }
        if (samples.length > length) samples = Arrays.copyOfRange(samples, 0, length);
        AL10.alBufferData(buffers.ids[nextBuffer], channels.channels == 1 ? 4353 : 4355, samples, sampleRate);
        OpenAlErrors.checkAlError();
        source.queueBuffer(buffers, nextBuffer);
        nextBuffer = (nextBuffer + 1) % buffers.ids.length;
    }
    public void delete() { unqueueProcessed(); source.delete(); }
}
