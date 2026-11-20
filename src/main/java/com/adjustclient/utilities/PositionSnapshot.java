package com.adjustclient.utilities;

public final class PositionSnapshot extends net.minecraft.util.Vec3 {
    public boolean onGround;
    public PositionSnapshot(boolean onGround, double x, double y, double z) {
        super(x, y, z);
        this.onGround = onGround;
    }
}
