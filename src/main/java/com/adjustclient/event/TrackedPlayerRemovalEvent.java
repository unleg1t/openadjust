package com.adjustclient.event;

public final class TrackedPlayerRemovalEvent extends Event {
    public final com.adjustclient.util.anticheat.player.TabPlayer player;

    public TrackedPlayerRemovalEvent(com.adjustclient.util.anticheat.player.TabPlayer player) {
        this.player = player;
    }
}
