package com.adjustclient.module.impl.movement.speed;

import com.adjustclient.Adjust;
import com.adjustclient.event.IncomingVelocityEvent;
import com.adjustclient.module.impl.movement.step.StepModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.SimulationPlayer;
import com.adjustclient.util.WorldUtil;
import java.util.function.Predicate;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class NcpSpeedMode extends SpeedMode<NcpSpeedState> {
    public final BooleanSetting fast;
    public final BooleanSetting damageBoost;
    public final NumberSetting jumpTicks;
    public boolean keepMovement;

    public NcpSpeedMode(SpeedModule module) {
        super(module, com.adjustclient.anticheat.feature.Features.SPEED_NCP);
        java.util.function.BooleanSupplier hidden = () -> module.mode.getValue() != this;
        fast = new BooleanSetting(module, "Fast", false, hidden);
        fast.setDescription("Makes the speed faster, does not work on Updated-NCP.");
        fast.setDisplayName("ncpFast");
        damageBoost = new BooleanSetting(module, "Damage Boost", false, hidden);
        damageBoost.setDisplayName("ncpDamageBoost");
        jumpTicks = new NumberSetting(module, "Jump Ticks", 10, 8, 11, 1, hidden);
        jumpTicks.setDisplayName("ncpJumpTicks");
    }

    @Override public void update(NcpSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (MovementUtil.isMovementRestricted(entity) || StepModule.INSTANCE.isStepping()) {
            reset(state);
            return;
        }
        if (!MovementUtil.isMoving()
                && !(state.jumpTicks == 7 && entity.offGroundTicks >= 2 && entity.offGroundTicks < 5 && !state.field39)) {
            reset(state);
            if (!MovementUtil.isMoving() && (entity.motionX != 0.0 || entity.motionZ != 0.0)) {
                double multiplier = 0.0;
                if (WorldUtil.isOverVoid()) multiplier = 1.0;
                else if (hasSupportAhead(entity)) state.jumped = true;
                entity.motionX *= multiplier;
                entity.motionZ *= multiplier;
            }
            return;
        }
        if (waitAfterCorrection(state)) return;
        updateHop(state);
        if (state.pendingSpeed != null) {
            strafe(entity, state.pendingSpeed);
            state.pendingSpeed = null;
        }
        if (state.jumped) return;
        if (jumpTicks.getInt() == 11) state.field39 = true;
        applyLowHop(state);
    }

    public void adjustJumpMotion(NcpSpeedState state, EntityPlayer entity) {
        if (state.field39) return;
        if (entity.isPotionActive(Potion.jump)) {
            int halfLevel = (entity.getActivePotionEffect(Potion.jump).getAmplifier() + 1) / 2;
            int tick = entity.offGroundTicks - halfLevel;
            if (tick == 5) entity.motionY -= 0.175;
            else if (tick == 7 && halfLevel >= 2) entity.motionY -= 0.15;
        } else {
            int tick = entity.offGroundTicks;
            switch (state.jumpTicks) {
                case 8:
                    if (tick == 1) entity.motionY += 0.0569;
                    else if (tick == 3) entity.motionY -= 0.1309;
                    else if (tick == 4) entity.motionY -= 0.2;
                    break;
                case 9:
                    if (tick == 5) entity.motionY -= 0.175;
                    break;
                case 10:
                    if (tick == 5) entity.motionY -= 0.075;
                    break;
                case 69:
                    if (tick == 4) entity.motionY -= 0.045;
                    else if (tick == 5) entity.motionY -= 0.175;
                    break;
            }
        }
    }

    public void onVelocity(NcpSpeedState state, IncomingVelocityEvent event, S12PacketEntityVelocity packet) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (!damageBoost.isEnabled() || entity.offGroundTicks < 2 || entity.fallDistance > 2.5F
                || Adjust.movementPredictor.historyTicks < 61) return;
        Vec3 velocity = new Vec3(packet.getMotionX() / 8000.0, packet.getMotionY() / 8000.0, packet.getMotionZ() / 8000.0);
        Simulation simulation = new Simulation(entity);
        SimulationPlayer simulated = simulation.player;
        simulated.motionX = velocity.xCoord;
        simulated.motionY = velocity.yCoord;
        simulated.motionZ = velocity.zCoord;
        for (int tick = 0; tick < 11; ++tick) {
            simulated.tick();
            if (tick == 0) {
                if (simulated.onGround) return;
                double speed = Math.hypot(simulated.motionX, simulated.motionZ);
                if (MovementUtil.getSpeed() > speed) return;
                strafe(simulated, speed);
            }
            if (simulated.onGround) break;
            float maximumFall = WorldUtil.isFreeAt(new Vec3(simulated.posX, simulated.posY, simulated.posZ), 0.0) ? 1.5F : 3.0F;
            if (simulated.fallDistance > maximumFall) return;
        }
        if (state.pendingSpeed != null) return;
        state.pendingSpeed = Math.hypot(packet.getMotionX() / 8000.0, packet.getMotionZ() / 8000.0);
        event.suppressApplication = true;
    }

    public void reset(NcpSpeedState state) {
        state.damaged = false;
        state.speed = 0.0;
        state.ticks = 0;
        state.pendingSpeed = null;
    }

    @Override public String getName() { return "NCP"; }

    public boolean canRun(NcpSpeedState state) { return checkMovement(state); }

    public boolean hasSupportAhead(Entity entity) {
        double distance = Math.max(0.5, Math.hypot(entity.motionX, entity.motionZ));
        double degrees = Math.atan2(-entity.motionX, entity.motionZ) * 57.29577951308232;
        double radians = degrees * 0.0174532925199433;
        double x = -Math.sin(radians) * distance + entity.posX;
        double z = Math.cos(radians) * distance + entity.posZ;
        return WorldUtil.isFreeAt(new Vec3(x, entity.posY, z), 0.0);
    }

    public boolean waitAfterCorrection(NcpSpeedState state) {
        if (Adjust.movementPredictor.historyTicks >= 4 || state.entity.isCollidedHorizontally) {
            state.ticks = 0;
            return false;
        }
        if (!mc.thePlayer.onGround) return true;
        if (Adjust.movementPredictor.historyTicks < 3) return true;
        if (state.ticks++ < 2) {
            strafe(state.entity, MovementUtil.getSpeedWithPotion(0.2873, 0.2) / 1.27);
            return true;
        }
        return false;
    }

    public void updateHop(NcpSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        double base = MovementUtil.getSpeedWithPotion(0.2873, 0.2);
        double initial = mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.moveSpeed) ? base * 1.18 - 0.01 : 0.329014;
        if (entity.offGroundTicks == 0) {
            if (MovementUtil.isMoving()) {
                if (!state.damaged) {
                    state.damaged = true;
                    state.speed = initial;
                }
                state.jumped = false;
                if (state.field38) {
                    entity.jump();
                    state.field38 = false;
                } else if (entity.motionY <= 0.0) entity.jump();
                state.field39 = state.jumped || Adjust.movementPredictor.historyTicks < 41;
                state.speed *= 1.6;
                state.jumpTicks = jumpTicks.getInt();
            }
        } else if (entity.offGroundTicks == 1) {
            double multiplier = fast.isEnabled() ? 0.66 : 0.76;
            if (state.jumpTicks == 7 && entity.offGroundTicks >= 2 && entity.offGroundTicks < 5 && !state.field39)
                multiplier += 0.02;
            state.speed -= multiplier * (state.speed - base);
            state.damaged = false;
        } else state.speed = state.lastDistance - state.lastDistance / 159.0;
        state.speed = Math.max(state.speed, base);
        strafe(entity, state.speed);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void applyLowHop(NcpSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (state.field38) {
            if (entity.offGroundTicks >= 7) state.field39 = false;
            return;
        }
        BlockPos pos = entity instanceof SimulationPlayer
                ? new BlockPos(entity.posX, entity.posY + 0.5, entity.posZ)
                : new BlockPos(entity.posX + 0.5, entity.posY + 0.5, entity.posZ + 0.5);
        Block below = mc.theWorld == null ? null : mc.theWorld.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock();
        if (mc.theWorld != null && mc.theWorld.getBlockState(pos).getBlock() instanceof BlockCarpet
                && (below instanceof BlockFenceGate || below instanceof BlockFence)) {
            state.field39 = true;
            return;
        }
        double previousY = entity.motionY;
        adjustJumpMotion(state, entity);
        if (entity.motionY >= 0.0) return;
        Predicate<IBlockState> partialBlock = blockState -> {
            Block block = blockState.getBlock();
            return block instanceof BlockStairs || block instanceof BlockSlab && !((BlockSlab) block).isDouble()
                    || block == net.minecraft.init.Blocks.snow_layer || block == net.minecraft.init.Blocks.carpet;
        };
        if (BlockUtil.isColliding(entity.getEntityBoundingBox().addCoord(0.0, entity.motionY, 0.0), partialBlock)) {
            entity.motionY = previousY;
            state.field39 = true;
        }
    }

    public boolean checkMovement(NcpSpeedState state) {
        EntityPlayer entity = (EntityPlayer) state.entity;
        if (state.jumpTicks == 7 && entity.offGroundTicks >= 2 && entity.offGroundTicks < 5 && !state.field39) {
            keepMovement = true;
            return false;
        }
        return true;
    }

    @Override public NcpSpeedState createState(Entity entity) { return new NcpSpeedState(entity); }
}
