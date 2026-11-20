package com.adjustclient.core.events;

public final class ScoreboardEntryEvent extends Event {
    public net.minecraft.scoreboard.Score score;

    public ScoreboardEntryEvent(net.minecraft.scoreboard.Score score) {
        this.score = score;
    }
}
