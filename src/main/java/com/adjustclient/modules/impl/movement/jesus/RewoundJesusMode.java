package com.adjustclient.modules.impl.movement.jesus;

import com.adjustclient.core.events.BlockCollisionCandidateEvent;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.JumpEvent;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.core.events.MoveFlyingEvent;
import com.adjustclient.core.events.StepHeightEvent;
import com.adjustclient.core.events.WorldResetEvent;
import com.adjustclient.modules.impl.combat.criticals.mode.RewoundCriticalsMode;
import com.adjustclient.modules.impl.movement.flight.FlightModule;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.RandomUtil;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class RewoundJesusMode extends JesusMode {
    public static RewoundJesusMode INSTANCE;
    public int ticks;

    public RewoundJesusMode(JesusModule module) {
        super(module, "Rewound");
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent event) {
        if (!isActive()) return;
        ticks = 0;
    }

    @EventHandler
    public void onBlockCollision(BlockCollisionCandidateEvent event) {
        if (!isActive() || !(event.block instanceof BlockLiquid)) return;
        BlockPos pos = event.position;
        if (mc.thePlayer.posY < pos.getY() + 1 || event.contextFlag) return;
        event.collisionBoxes.add(new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D));
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (!isActive() || !event.pre) return;
        if (isStandingOnLiquid()) {
            applyHoverOffset(event, ticks);
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @EventHandler
    public void onMoveFlying(MoveFlyingEvent event) {
        if (!isActive() || event.pre) return;
        if (isStandingOnLiquid()) {
            Objects.requireNonNull(mc.thePlayer);
        }
    }

    @EventHandler
    public void onStepHeight(StepHeightEvent event) {
        if (isActive() && isStandingOnLiquid() && event.stepHeight > 0.6F) {
            event.stepHeight = 0.6F;
        }
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (isActive() && isStandingOnLiquid()) {
            event.cancelled = true;
        }
    }

    public static boolean isStandingOnLiquid() {
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().contract(0.305D, 0.0D, 0.305D).offset(0.0D, -0.1D, 0.0D);
        Block block = mc.theWorld.getBlockState(new BlockPos(box.minX, box.minY, box.minZ)).getBlock();
        if (!(block instanceof BlockLiquid)) return false;
        if (MovementUtil.isInLiquid()) return false;
        return !FlightModule.INSTANCE.isEnabled();
    }

    public static void applyHoverOffset(MotionEvent event, int tick) {
        int stage = tick % 5;
        float base = (stage == 0 || stage == 2) ? 0.000014F : (stage == 1 || stage == 3) ? 0.000024F : 0.000017F;
        event.y += base + RandomUtil.nextFloat(0.00000014F, 0.00000043F);
        RewoundCriticalsMode.INSTANCE.jumped = true;
    }
}
