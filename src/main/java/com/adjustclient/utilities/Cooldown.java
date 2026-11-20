package com.adjustclient.utilities;

public final class Cooldown {
    public Timer timer;
    public long durationMs;
    public Cooldown(long durationMs) { this.timer = new Timer(); this.durationMs = durationMs; }
    public Cooldown(long durationMs, Timer timer) { this.timer = timer; this.durationMs = durationMs; }
    public boolean isExpired(long extraMs) { return durationMs >= 0 && timer.hasElapsed(durationMs + extraMs); }
    public boolean isExpired() { return durationMs >= 0 && timer.hasElapsed(durationMs); }
}
