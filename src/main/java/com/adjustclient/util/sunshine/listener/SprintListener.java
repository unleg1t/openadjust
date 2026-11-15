package com.adjustclient.util.sunshine.listener;
import com.adjustclient.util.sunshine.tracker.PositionKey;
public interface SprintListener extends CheckListener {
    void onRotation(PositionKey from, PositionKey to);
}
