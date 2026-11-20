package com.adjustclient.core.events;

public final class BlockCollisionCandidateEvent extends CancellableEvent {
    public boolean contextFlag;
    public net.minecraft.block.Block block;
    public net.minecraft.util.BlockPos position;
    public net.minecraft.util.AxisAlignedBB boundingBox;
    public java.util.List<net.minecraft.util.AxisAlignedBB> collisionBoxes;

    public BlockCollisionCandidateEvent(net.minecraft.block.Block block, net.minecraft.util.BlockPos position,
            net.minecraft.util.AxisAlignedBB boundingBox,
            java.util.List<net.minecraft.util.AxisAlignedBB> collisionBoxes, boolean contextFlag) {
        this.block = block;
        this.position = position;
        this.boundingBox = boundingBox;
        this.collisionBoxes = collisionBoxes;
        this.contextFlag = contextFlag;
    }
}
