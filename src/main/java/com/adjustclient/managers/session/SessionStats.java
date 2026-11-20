package com.adjustclient.managers.session;

public final class SessionStats {
    public String name;
    public long firstSeen;
    public long lastSeen;
    public long hypixelTime;
    public long totalTime;
    public int kills;
    public int deaths;
    public int games;
    public Object field52;

    public SessionStats(String name, long firstSeen, long lastSeen) {
        this.name = name;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }
}
