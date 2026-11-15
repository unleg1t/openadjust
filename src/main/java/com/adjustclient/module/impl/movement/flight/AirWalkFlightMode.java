package com.adjustclient.module.impl.movement.flight;

import com.adjustclient.event.BlockCollisionCandidateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.MovementUtil;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class AirWalkFlightMode extends FlightMode {
    public static AirWalkFlightMode INSTANCE;
    public final BooleanSetting jump;

    public AirWalkFlightMode(FlightModule module) {
        super(module, "Air Walk");
        INSTANCE = this;
        jump = new BooleanSetting(module, "Jump", false, "On", "Off",
                () -> module.mode.getValue() != this);
    }

    @EventHandler public void onBlockCollision(BlockCollisionCandidateEvent event) {
        if (!isActive() || mc.gameSettings.keyBindSneak.isKeyDown() || MovementUtil.isMovementRestricted(mc.thePlayer)) return;
        if (jump.isEnabled() && mc.thePlayer.fallDistance <= 1.0F
                && !mc.gameSettings.keyBindJump.isKeyDown()) return;
        Block block = event.block;
        BlockPos pos = event.position;
        if (block == null || !block.isPassable(mc.theWorld, pos)) return;
        if (mc.thePlayer.posY > pos.getY()) {
            event.boundingBox = new AxisAlignedBB(pos.getX() - 2.0D, pos.getY() - 1.0D, pos.getZ() - 2.0D,
                    pos.getX() + 2.0D, pos.getY() + 1.0D, pos.getZ() + 2.0D);
        }
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isActive()) return;
        event.sneak = false;
        if (jump.isEnabled()) event.jump = true;
    }
}
