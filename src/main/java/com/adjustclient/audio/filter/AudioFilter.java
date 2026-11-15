package com.adjustclient.audio.filter;

public abstract class AudioFilter implements PcmFilter {
    public boolean supportsTail() { return false; }
    public void process(short[] samples, int offset, int length) {}
    public void reset() {}
    public void postProcess(short[] samples, int offset, int length) {}
    public void processTail(short[] samples, int offset, int length) {}
    public abstract boolean isFinished();
    public abstract boolean isActive();
}
