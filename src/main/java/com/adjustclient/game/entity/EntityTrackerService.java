package com.adjustclient.game.entity;

import com.adjustclient.event.Listener;
import com.adjustclient.game.entity.tracker.ServerPositionTracker;

public final class EntityTrackerService implements Listener {
    public final ServerPositionTracker tracker;
    public final TrackedPacketFilter filter;
    public Object field12;

    public EntityTrackerService(ServerPositionTracker tracker) {
        this.tracker = tracker;
        this.filter = new TrackedPacketFilter();
    }
}
