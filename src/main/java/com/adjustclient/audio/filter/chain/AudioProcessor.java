package com.adjustclient.audio.filter.chain;

public interface AudioProcessor {
    void processInput(short[] samples, int offset, int length);
    boolean isActive();
    void process(short[] samples, int offset, int length);
    boolean hasTail();
}
