package com.adjustclient.util.sunshine.listener;
import com.adjustclient.util.sunshine.tracker.TrackedPosition;
public interface MotionListener extends CheckListener {
    void onJump(TrackedPosition from, TrackedPosition to, double motion);
}
