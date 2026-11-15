package com.adjustclient.util;

public class Timer {
    public long lastMs = System.currentTimeMillis();

    public Timer() {}
    public Timer(long lastMs) { this.lastMs = lastMs; }

    public void reset() { lastMs = System.currentTimeMillis(); }

    public String formatElapsed() {
        long elapsed = System.currentTimeMillis() - lastMs;
        return elapsed < 10000L ? elapsed + "ms" : TimeUtil.formatDuration(elapsed, true);
    }

    public boolean hasElapsed(long millis) { return lastMs + millis <= System.currentTimeMillis(); }
}
