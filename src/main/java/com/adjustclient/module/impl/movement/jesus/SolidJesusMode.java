package com.adjustclient.module.impl.movement.jesus;

import com.adjustclient.event.BlockCollisionCandidateEvent;
import com.adjustclient.event.EventHandler;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class SolidJesusMode extends JesusMode {

    public SolidJesusMode(JesusModule module) {
        super(module, "Solid");
    }

    @EventHandler
    public void onBlockCollision(BlockCollisionCandidateEvent event) {
        if (!isActive() || !(event.block instanceof BlockLiquid)) return;
        BlockPos pos = event.position;
        if (mc.thePlayer.posY < pos.getY() + 1 || event.contextFlag) return;
        event.collisionBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0));
    }
}
