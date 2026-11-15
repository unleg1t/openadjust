package com.adjustclient.module.impl.movement.antivoid;

import net.minecraft.client.entity.EntityPlayerSP;

public class MovementStateSnapshot {
    public final float lastReportedYaw;
    public final double lastReportedX;
    public final double lastReportedY;
    public final double lastReportedZ;
    public final float lastReportedPitch;
    public final int positionUpdateTicks;
    public final boolean serverSprintState;

    public MovementStateSnapshot(EntityPlayerSP player) {
        lastReportedYaw = player.lastReportedYaw;
        lastReportedX = player.lastReportedPosX;
        lastReportedY = player.lastReportedPosY;
        lastReportedZ = player.lastReportedPosZ;
        lastReportedPitch = player.lastReportedPitch;
        positionUpdateTicks = player.positionUpdateTicks;
        serverSprintState = player.serverSprintState;
    }
}
