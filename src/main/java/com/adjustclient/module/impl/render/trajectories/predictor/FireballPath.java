package com.adjustclient.module.impl.render.trajectories.predictor;
import com.adjustclient.module.impl.render.trajectories.TrajectoryPath;
import java.util.List;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
public final class FireballPath extends TrajectoryPath {
    public final Map<BlockPos, IBlockState> affectedBlocks;
    public final List<Entity> affectedEntities;
    public FireballPath(List<Vec3> points, MovingObjectPosition hit, Map<BlockPos, IBlockState> blocks, List<Entity> entities) {
        super(points, hit); affectedBlocks = blocks; affectedEntities = entities;
    }
}
