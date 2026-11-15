package com.adjustclient.audio.device;

public enum ChannelMode {
    MONO(1), STEREO(2);
    public final int channels;
    ChannelMode(int channels) { this.channels = channels; }
    public String field20;
}
