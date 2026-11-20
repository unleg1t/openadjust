package com.adjustclient.core.audio.filter.chain;

import java.util.ArrayList;
import java.util.List;
import com.adjustclient.core.audio.filter.AudioFilter;
public class AudioFilterChain implements AudioProcessor {
    public List<AudioFilter> filters = new ArrayList<>();
    public void processInput(short[] samples, int offset, int length) {
        for (AudioFilter filter : filters) if (filter.isActive()) filter.process(samples, 0, length);
        for (AudioFilter filter : filters) if (filter.isActive()) filter.postProcess(samples, 0, length);
    }
    public boolean isActive() {
        for (AudioFilter filter : filters) if (!filter.isFinished() && filter.isActive()) return false;
        return true;
    }
    public void process(short[] samples, int offset, int length) {
        for (AudioFilter filter : filters) if (filter.supportsTail() && filter.isActive()) filter.processTail(samples, 0, length);
    }
    public boolean hasTail() {
        for (AudioFilter filter : filters) if (filter.supportsTail()) return true;
        return false;
    }
}
