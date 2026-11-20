package com.adjustclient.core.audio.device;

import java.util.Objects;
public final class AudioFormat {
    public ChannelMode channels;
    public int sampleRate;
    public String field12;
    public AudioFormat(ChannelMode channels, int sampleRate) { this.channels = channels; this.sampleRate = sampleRate; }
    public boolean equals(Object other) { return this == other || other instanceof AudioFormat f && getClass() == other.getClass() && channels == f.channels && sampleRate == f.sampleRate; }
    public int hashCode() { return Objects.hash(sampleRate, channels); }
}
