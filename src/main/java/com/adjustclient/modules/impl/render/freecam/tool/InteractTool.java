package com.adjustclient.modules.impl.render.freecam.tool;

import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.Vec2f;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class InteractTool extends FreecamTool {
    public Object field16;

    public InteractTool(FreecamController controller, MovingObjectPosition hit) {
        super(controller, hit);
        field12 = true;
    }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) {
        BlockPos block = hit.getBlockPos();
        double reach = mc.playerController.isInCreativeMode() ? 5.0D : 4.5D;
        Vec3 eyes = position.addVector(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        Vec2f angles = RotationUtil.getYawBetween(new Vec3(block).addVector(0.5D, 0.5D, 0.5D), eyes);
        float yaw = -angles.x * 0.017453292F - (float) Math.PI;
        float pitch = -angles.y * 0.017453292F;
        float horizontal = -net.minecraft.util.MathHelper.cos(pitch);
        double x = net.minecraft.util.MathHelper.sin(yaw) * horizontal;
        double y = net.minecraft.util.MathHelper.sin(pitch);
        double z = net.minecraft.util.MathHelper.cos(yaw) * horizontal;
        MovingObjectPosition ray = mc.theWorld.rayTraceBlocks(eyes,
                eyes.addVector(x * reach, y * reach, z * reach), false, false, true);
        net.minecraft.util.EnumFacing side = ray != null
                && ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && block.equals(ray.getBlockPos()) ? ray.sideHit : hit.sideHit;
        packets.add(new C03PacketPlayer.C05PacketPlayerLook(angles.x, angles.y, false));
        packets.add(new C0APacketAnimation());
        packets.add(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, block, side));
        controller.recordBlock(block);
        mc.theWorld.setBlockState(block, Blocks.air.getDefaultState(), 3);
    }
}
