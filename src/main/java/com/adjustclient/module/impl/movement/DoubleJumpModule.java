package com.adjustclient.module.impl.movement;

import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.JumpEvent;
import com.adjustclient.event.JumpEligibilityEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.BlockCollisionCandidateEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MovementUtil;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class DoubleJumpModule extends Module {
    public static DoubleJumpModule INSTANCE;
    public final ModeSetting<DoubleJumpMode> mode;
    public final BooleanSetting noAscend;
    public int jumpTicks;
    public int airJumps;
    private int lastJumpTick;

    public DoubleJumpModule() {
        super("Double Jump", Category.MOVEMENT);
        INSTANCE = this;
        DoubleJumpMode vanilla = (DoubleJumpMode) DoubleJumpMode.VANILLA;
        DoubleJumpMode mmc = (DoubleJumpMode) DoubleJumpMode.MMC;
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, mmc);
        noAscend = new BooleanSetting(this, "No Ascend", false,
                () -> mode.getValue() != mmc);
    }

    @EventHandler public void onBlockCollision(BlockCollisionCandidateEvent event) {
        if (!isEnabled() || mode.getValue() != DoubleJumpMode.MMC
                || !com.adjustclient.anticheat.feature.Features.DOUBLE_JUMP_MMC.isAllowed() || airJumps >= 2) return;
        boolean jump = mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown();
        AxisAlignedBB below = mc.thePlayer.getEntityBoundingBox().expand(-0.1D, 0.0D, -0.1D)
                .addCoord(0.0D, -0.08D, 0.0D);
        if (!jump || MovementUtil.isMovementRestricted(mc.thePlayer)
                || mc.thePlayer.fallDistance <= (noAscend.isEnabled() ? 1.0F : 0.0F)
                || BlockUtil.isColliding(below)) return;
        Block block = event.block;
        BlockPos pos = event.position;
        if (block != null && block.isPassable(mc.theWorld, pos) && mc.thePlayer.posY > pos.getY()) {
            event.boundingBox = new AxisAlignedBB(pos.getX() - 2.0D, pos.getY() - 1.0D, pos.getZ() - 2.0D,
                    pos.getX() + 2.0D, pos.getY() + 1.0D, pos.getZ() + 2.0D);
            jumpTicks = 2;
        }
    }

    @EventHandler public void onLivingUpdate(JumpEligibilityEvent event) {
        if (!isEnabled()) return;
        if (mode.getValue() == DoubleJumpMode.VANILLA) {
            if (com.adjustclient.anticheat.feature.Features.DOUBLE_JUMP_VANILLA.isAllowed() && event.jumpTicks < 1)
                event.onGround = true;
        } else if (jumpTicks >= 1) {
            event.jumpTicks = 0;
        }
    }

    @EventHandler public void onJump(JumpEvent event) {
        if (!isEnabled()) return;
        if (jumpTicks >= 1 && lastJumpTick != mc.thePlayer.ticksExisted) ++airJumps;
        lastJumpTick = mc.thePlayer.ticksExisted;
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        AxisAlignedBB below = mc.thePlayer.getEntityBoundingBox().expand(-0.1D, 0.0D, -0.1D)
                .addCoord(0.0D, -0.08D, 0.0D);
        if (BlockUtil.isColliding(below)) {
            airJumps = 0;
            return;
        }
        if (mc.thePlayer.onGround && airJumps >= 2 && ++airJumps >= 4) airJumps = 0;
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }
}
