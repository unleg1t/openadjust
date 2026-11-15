package com.adjustclient.audio.openal;

public enum AlPcmFormat {
    MONO_8(4352), MONO_16(4353), STEREO_8(4354), STEREO_16(4355);
    public final int value;
    AlPcmFormat(int value) { this.value = value; }
    public String field20;
}
