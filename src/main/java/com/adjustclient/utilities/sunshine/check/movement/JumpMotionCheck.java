package com.adjustclient.utilities.sunshine.check.movement;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.MotionListener;
import com.adjustclient.utilities.sunshine.tracker.TrackedPosition;
import com.adjustclient.utilities.sunshine.CheckCategory;
public class JumpMotionCheck extends SunshineCheck implements MotionListener {
    public JumpMotionCheck() { super("Motion", "Detects modified jump motion (0.42D)"); }
    @Override public void onJump(TrackedPosition from, TrackedPosition to, double motion) {
        if (motion == 0.4199999868869781) return;
        fail(new ViolationDetails(motion == 0.42 ? CheckCategory.KICK : CheckCategory.ALERT)
                .put("motion", motion).put("from", from).put("to", to));
    }
}
