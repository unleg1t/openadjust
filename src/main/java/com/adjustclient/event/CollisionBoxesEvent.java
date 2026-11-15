package com.adjustclient.event;

public class CollisionBoxesEvent extends Event {
    public java.util.List<net.minecraft.util.AxisAlignedBB> collisionBoxes;

    public CollisionBoxesEvent(java.util.List<net.minecraft.util.AxisAlignedBB> collisionBoxes) {
        this.collisionBoxes = collisionBoxes;
    }
}
