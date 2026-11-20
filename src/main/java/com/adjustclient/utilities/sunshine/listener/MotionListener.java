package com.adjustclient.utilities.sunshine.listener;
import com.adjustclient.utilities.sunshine.tracker.TrackedPosition;
public interface MotionListener extends CheckListener {
    void onJump(TrackedPosition from, TrackedPosition to, double motion);
}
