package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.util.Vec2f;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class RotationCandidate {

    public Vec3 hitVec;
    public BlockPos pos;
    public Vec2f rotation;
    public Block block;

    public RotationCandidate(Vec3 hitVec, BlockPos pos, Vec2f rotation, Block block) {
        this.hitVec = hitVec;
        this.pos = pos;
        this.rotation = rotation;
        this.block = block;
    }
}
