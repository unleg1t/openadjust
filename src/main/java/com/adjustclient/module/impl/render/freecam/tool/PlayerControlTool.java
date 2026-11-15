package com.adjustclient.module.impl.render.freecam.tool;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class PlayerControlTool extends FreecamTool {
    public Object field16;

    public PlayerControlTool(FreecamController controller, MovingObjectPosition hit) { super(controller, hit); }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) {
        BlockPos block = hit.getBlockPos();
        Vec3 relative = hit.hitVec.subtract(new Vec3(block));
        packets.add(new C0APacketAnimation());
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        packets.add(new C08PacketPlayerBlockPlacement(held == null ? null : held.copy()));
        held = mc.thePlayer.inventory.getCurrentItem();
        packets.add(new C08PacketPlayerBlockPlacement(block, hit.sideHit.getIndex(),
                held == null ? null : held.copy(), (float) relative.xCoord, (float) relative.yCoord,
                (float) relative.zCoord));
        controller.module.camera.swingItem();
    }
}
