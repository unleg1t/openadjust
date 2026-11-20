package com.adjustclient.modules.impl.combat.antibot;

import com.adjustclient.utilities.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public final class BotMovementTracker {
    public final Entity entity;
    public Vec3 lastPosition;
    public final Timer movementTimer = new Timer();
    public final Timer teleportTimer = new Timer();
    public final int entityId;
    public double squaredMovement;
    public int relativeMoves;
    public int teleports;
    public int verdict;
    public short age;
    public byte field46;
    public byte emptyMoveStreak;
    public boolean metadataSeen;
    public boolean nonLookMoveSeen;
    boolean field50;
    boolean field51;

    BotMovementTracker(Entity entity) {
        this.entity = entity;
        this.entityId = entity.getEntityId();
    }

    void updatePosition(Entity entity) {
        Vec3 old = lastPosition;
        lastPosition = entity.getPositionVector();
        if (old != null) {
            squaredMovement += lastPosition.squareDistanceTo(old);
        }
    }
}
