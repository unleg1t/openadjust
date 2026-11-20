package com.adjustclient.utilities.sunshine.check.packet;
public class ExpectedTeleport extends ExpectedReply {
    public final float yaw;
    public final double x;
    public final double y;
    public final double z;
    public final float pitch;
    public final boolean ground;
    public Object field40;
    public ExpectedTeleport(double x, double y, double z, float yaw, float pitch, boolean ground) {
        this.x=x; this.y=y; this.z=z; this.yaw=yaw; this.pitch=pitch; this.ground=ground;
    }
    @Override public boolean matches(ExpectedReply other) {
        if (!(other instanceof ExpectedTeleport)) return false;
        ExpectedTeleport t = (ExpectedTeleport) other;
        return Double.compare(x,t.x)==0 && Double.compare(y,t.y)==0 && Double.compare(z,t.z)==0
                && Float.compare(yaw,t.yaw)==0 && Float.compare(pitch,t.pitch)==0;
    }
    @Override public String toString() {
        return "Teleport{x="+x+", y="+y+", z="+z+", yaw="+yaw+", pitch="+pitch+", ground="+ground+"}";
    }
}
