package com.adjustclient.util.anticheat.player;

public final class KillEvent {
    public final String victim;
    public final String killer;
    public final boolean finalKill;
    public Object field16;
    public KillEvent(String victim, String killer, boolean finalKill) {
        this.victim = victim;
        this.killer = killer;
        this.finalKill = finalKill;
    }
}
