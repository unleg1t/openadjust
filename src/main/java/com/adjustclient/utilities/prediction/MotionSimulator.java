package com.adjustclient.utilities.prediction;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.MovementUtil;
import net.minecraft.block.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
public final class MotionSimulator implements MinecraftInstance {
    public Vec3 previous;
    public Vec3 position;
    public boolean onGround;
    public boolean field13;
    public boolean field14;
    public double speed;
    public double baseSpeed = 0.287;
    public Object field32;
    public MotionSimulator() {}
    public MotionSimulator(Vec3 previous, Vec3 position, boolean onGround, boolean stairs, double speed) {
        this.previous = previous; this.position = position; this.onGround = onGround; this.field14 = stairs; this.speed = speed;
    }
    public boolean isOnStairs(Vec3 point) {
        AxisAlignedBB bounds = new AxisAlignedBB(point.xCoord - 0.300000011920929,
                point.yCoord - 0.00006500000017695129, point.zCoord - 0.300000011920929,
                point.xCoord + 0.300000011920929, point.yCoord + 1.799999952316284, point.zCoord + 0.300000011920929);
        for (AxisAlignedBB collision : mc.theWorld.getCollisionBoxes(bounds)) {
            if (point.yCoord < collision.minY) continue;
            BlockPos pos = new BlockPos(collision.minX, collision.minY, collision.minZ);
            if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockStairs) return true;
        }
        return false;
    }
    public void update(Vec3 from, Vec3 to) {
        double dx = Math.abs(from.xCoord - to.xCoord), dz = Math.abs(from.zCoord - to.zCoord);
        speed = Math.sqrt(dx * dx + dz * dz);
        field14 = isOnStairs(from);
        AxisAlignedBB bounds = new AxisAlignedBB(to.xCoord - 0.300000011920929,
                to.yCoord - 0.015625, to.zCoord - 0.300000011920929,
                to.xCoord + 0.300000011920929, to.yCoord + 1.799999952316284, to.zCoord + 0.300000011920929);
        onGround = false;
        for (AxisAlignedBB collision : mc.theWorld.getCollisionBoxes(bounds)) {
            if (to.yCoord >= collision.minY) { onGround = true; break; }
        }
        isOnIce(to);
        field13 = hasBlockTwoBelow(to);
        MovementUtil.getSpeedWithPotion(baseSpeed, 0.2);
        MovementUtil.getSpeed();
        previous = from;
        position = to;
    }
    public boolean hasBlockTwoBelow(Vec3 point) {
        BlockPos pos = new BlockPos(point.xCoord, point.yCoord, point.zCoord).up(2);
        AxisAlignedBB collision = BlockUtil.getCollisionBox(pos);
        return collision != null && collision.maxY - collision.minY != 0.0;
    }
    public boolean isOnIce(Vec3 point) {
        BlockPos pos = new BlockPos(point.xCoord, point.yCoord - (onGround ? 0.6 : 1.1), point.zCoord);
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block.getClass() == BlockIce.class || block.getClass() == BlockPackedIce.class;
    }
}
