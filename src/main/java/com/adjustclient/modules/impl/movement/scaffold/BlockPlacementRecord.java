package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.utilities.Timer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class BlockPlacementRecord {

    public BlockPos pos;
    public EnumFacing facing;
    public net.minecraft.block.Block block;
    public IBlockState state;
    public Timer placedTime;
    public Timer confirmedTime;
    public Vec3 playerPosition;
    public BlockPos newBlockPos;
    public int heldSlot;
    public boolean confirmed;
    public boolean accepted;
    public boolean failureReported;

    public BlockPlacementRecord(BlockPos pos, EnumFacing facing, net.minecraft.block.Block block,
                                IBlockState state, Vec3 playerPosition, int heldSlot) {
        this.pos = pos;
        this.facing = facing;
        this.block = block;
        this.state = state;
        this.playerPosition = playerPosition;
        this.newBlockPos = com.adjustclient.utilities.BlockUtil.getPlacePos(pos, facing);
        this.heldSlot = heldSlot;
        this.placedTime = new Timer();
        this.confirmedTime = new Timer();
        this.confirmedTime.lastMs = 0L;
    }
}
