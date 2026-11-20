package com.adjustclient.utilities.sunshine.check.movement;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.MovementListener;
import com.adjustclient.utilities.sunshine.tracker.TrackedPosition;
import com.adjustclient.utilities.sunshine.CheckCategory;
public class GroundCheck extends SunshineCheck implements MovementListener {
    public GroundCheck() { super("Ground", "Spoofed ground checks"); }
    @Override public void onMovement(TrackedPosition from, TrackedPosition to) {
        if (from == null || from.teleport || to.teleport) return;
        if (to.ground != (to.yCoord % 0.015625 != 0.0)) return;
        if (anticheat.positionTracker.flying) return;
        double dy = to.yCoord - from.yCoord;
        if (dy >= 0.5 && dy % 0.015625 == 0.0 && dy < 1.0) return;
        fail(new ViolationDetails(CheckCategory.ALERT).put("from", from).put("to", to));
    }
}
