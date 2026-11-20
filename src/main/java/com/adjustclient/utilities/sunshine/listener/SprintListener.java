package com.adjustclient.utilities.sunshine.listener;
import com.adjustclient.utilities.sunshine.tracker.PositionKey;
public interface SprintListener extends CheckListener {
    void onRotation(PositionKey from, PositionKey to);
}
