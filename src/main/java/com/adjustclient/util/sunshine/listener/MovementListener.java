package com.adjustclient.util.sunshine.listener;
import com.adjustclient.util.sunshine.tracker.TrackedPosition;
public interface MovementListener extends CheckListener {
    void onMovement(TrackedPosition from, TrackedPosition to);
}
