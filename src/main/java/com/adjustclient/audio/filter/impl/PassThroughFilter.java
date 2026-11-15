package com.adjustclient.audio.filter.impl;

import com.adjustclient.audio.filter.AudioFilter;

public final class PassThroughFilter extends AudioFilter {
    public boolean enabled;
    public String name;
    public void process(short[] samples, int offset, int length) {}
    public boolean isFinished() { return true; }
    public boolean isActive() { return false; }
}
