package com.adjustclient.game.entity.tracker;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

public final class ServerPositionEntry {
    public Entity entity;
    public AxisAlignedBB boundingBox;
    public float yaw;
    public double x;
    public double y;
    public double z;
    public double previousX;
    public double previousY;
    public double previousZ;
    public double targetX;
    public double targetY;
    public double targetZ;
    public double targetYaw;
    public double targetPitch;
    public double previousChasingX;
    public double previousChasingY;
    public double previousChasingZ;
    public double chasingX;
    public double chasingY;
    public double chasingZ;
    public float pitch;
    public float previousYaw;
    public float previousPitch;
    public float renderYawOffset;
    public float previousRenderYawOffset;
    public float headYaw;
    public float previousHeadYaw;
    public int serverX;
    public int serverY;
    public int serverZ;
    public int interpolationSteps;
    public Object field196;
}
