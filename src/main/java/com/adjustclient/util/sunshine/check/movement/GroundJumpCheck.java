package com.adjustclient.util.sunshine.check.movement;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.MotionListener;
import com.adjustclient.util.sunshine.tracker.TrackedPosition;
import com.adjustclient.util.sunshine.CheckCategory;
public final class GroundJumpCheck extends SunshineCheck implements MotionListener {
    public GroundJumpCheck() { super("Ground Jump", "Detects air jumps"); }
    @Override public void onJump(TrackedPosition from, TrackedPosition to, double motion) {
        if (from.yCoord % 0.015625 == 0.0) return;
        fail(new ViolationDetails(CheckCategory.ALERT).put("motion", motion).put("from", from).put("to", to));
    }
}
