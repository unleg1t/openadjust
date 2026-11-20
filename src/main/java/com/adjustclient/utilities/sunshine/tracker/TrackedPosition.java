package com.adjustclient.utilities.sunshine.tracker;
import com.adjustclient.utilities.StringUtil;
import java.util.Objects;
import net.minecraft.util.Vec3;
public final class TrackedPosition extends Vec3 {
    public final PositionKey rotation;
    public final boolean ground;
    public final boolean teleport;
    public Object field36;
    public TrackedPosition(PositionKey rotation, double x, double y, double z, boolean ground, boolean teleport) {
        super(x, y, z); this.rotation = rotation; this.ground = ground; this.teleport = teleport;
    }
    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != TrackedPosition.class) return false;
        TrackedPosition p = (TrackedPosition) other;
        return Double.compare(xCoord, p.xCoord) == 0 && Double.compare(yCoord, p.yCoord) == 0
                && Double.compare(zCoord, p.zCoord) == 0 && ground == p.ground;
    }
    @Override public int hashCode() { return Objects.hash(super.hashCode(), ground); }
    @Override public String toString() {
        return "Position{x=" + StringUtil.THREE_DECIMALS.format(xCoord) + ",y=" + yCoord
                + ",z=" + StringUtil.THREE_DECIMALS.format(zCoord) + (ground ? ",ground" : "")
                + (teleport ? ",tp" : "") + "}";
    }
}
