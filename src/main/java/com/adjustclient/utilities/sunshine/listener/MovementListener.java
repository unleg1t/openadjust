package com.adjustclient.utilities.sunshine.listener;
import com.adjustclient.utilities.sunshine.tracker.TrackedPosition;
public interface MovementListener extends CheckListener {
    void onMovement(TrackedPosition from, TrackedPosition to);
}
