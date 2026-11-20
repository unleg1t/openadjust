package com.adjustclient.core.events;

public final class TrackedPlayerRemovalEvent extends Event {
    public final com.adjustclient.utilities.anticheat.player.TabPlayer player;

    public TrackedPlayerRemovalEvent(com.adjustclient.utilities.anticheat.player.TabPlayer player) {
        this.player = player;
    }
}
