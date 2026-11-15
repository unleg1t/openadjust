package com.adjustclient.util.sunshine.tracker;
import java.util.Objects;
public final class PositionKey {
    public final float yaw;
    public final float pitch;
    public Object field12;
    public PositionKey(float yaw, float pitch) { this.yaw = yaw; this.pitch = pitch; }
    @Override public boolean equals(Object other) {
        if (other == null || other.getClass() != PositionKey.class) return false;
        PositionKey key = (PositionKey) other;
        return Float.compare(yaw, key.yaw) == 0 && Float.compare(pitch, key.pitch) == 0;
    }
    @Override public int hashCode() { return Objects.hash(yaw, pitch); }
}
