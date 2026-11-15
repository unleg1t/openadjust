package com.adjustclient.audio.filter;

public interface PcmFilter {
    boolean supportsTail();
    void process(short[] samples, int offset, int length);
    void postProcess(short[] samples, int offset, int length);
    void processTail(short[] samples, int offset, int length);
    void reset();
    boolean isFinished();
    boolean isActive();
}
