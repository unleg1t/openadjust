package com.adjustclient.event;

import com.adjustclient.Adjust;
import com.adjustclient.game.entity.tracker.ServerPositionEntry;
import com.adjustclient.game.entity.tracker.ServerPositionTracker;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import com.adjustclient.module.impl.combat.backtrack.BackTrackModule;

public final class EntityBoundsEvent extends Event {
    public Object field4;
    public Object field8;

    public static AxisAlignedBB getRenderBoundingBox(Entity entity) {
        AxisAlignedBB box = entity.getEntityBoundingBox();
        ServerPositionTracker tracker = ServerPositionTracker.INSTANCE;
        if (tracker == null) return box;
        Objects.requireNonNull(Adjust.entityTrackerService);
        Objects.requireNonNull(BackTrackModule.INSTANCE);
        boolean suppressed = BackTrackModule.INSTANCE.target != null;
        if (!suppressed && !tracker.trackingActive) {
            tracker.resyncPositions();
        }
        tracker.trackingActive = !suppressed;
        if (suppressed || entity.ridingEntity != null) return box;
        ServerPositionEntry entry = (ServerPositionEntry) tracker.entries.get(entity.getEntityId());
        if (entry == null) return box;
        if (entry.entity == entity) return entry.boundingBox;
        synchronized (tracker.entries) {
            if (tracker.entries.get(entity.getEntityId()) == entry) {
                tracker.entries.remove(entity.getEntityId());
            }
        }
        return box;
    }
}
