package com.adjustclient.audio.openal;

public enum AlSourceState {
    INITIAL(4113), PLAYING(4114), PAUSED(4115), STOPPED(4116);
    public final int value;
    AlSourceState(int value) { this.value = value; }
}
