package com.adjustclient.util;

import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.module.impl.combat.targetstrafe.TargetStrafeModule;

public final class MovementUtil implements MinecraftInstance {
    public static final Supplier<Float> INPUT_DIRECTION = () -> {
        return getDirection(RenderRotationTracker.INSTANCE.rotation == null
                ? mc.thePlayer.rotationYaw : RenderRotationTracker.INSTANCE.rotation.x);
    };
    public static float getDirection(float yaw) {
        float forward = (mc.gameSettings.keyBindForward.isKeyDown() ? 1 : 0)
                - (mc.gameSettings.keyBindBack.isKeyDown() ? 1 : 0);
        float strafe = (mc.gameSettings.keyBindLeft.isKeyDown() ? 1 : 0)
                - (mc.gameSettings.keyBindRight.isKeyDown() ? 1 : 0);
        return getDirection(yaw, forward, strafe);
    }
    public static double getJumpMotion(double base) {
        if (mc.thePlayer.isPotionActive(Potion.jump)) return base + (double) ((mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
        return base;
    }
    public static void setSpeed(double speed) {
        if (mc.thePlayer == null) return;
        setSpeed(true, speed, Math.toRadians(getDirection()));
    }
    public static double getSpeedWithPotion(double baseSpeed, double perLevel) {
        if (mc.thePlayer == null) return baseSpeed;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            baseSpeed *= 1 + perLevel * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }
    public static void stop() {
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
    }
    public static float getDirection(float yaw, float forward, float strafe) {
        if (forward < 0) yaw += 180;
        float offset = forward < 0 ? -45 : forward > 0 ? 45 : 90;
        if (strafe > 0) yaw -= offset;
        if (strafe < 0) yaw += offset;
        return yaw;
    }
    public static double getSpeed() {
        return mc.thePlayer == null ? 0 : Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }
    public static void center(double maxMotion) {
        mc.thePlayer.motionX = Math.clamp(Math.floor(mc.thePlayer.posX) + 0.5 - mc.thePlayer.posX, -maxMotion, maxMotion);
        mc.thePlayer.motionZ = Math.clamp(Math.floor(mc.thePlayer.posZ) + 0.5 - mc.thePlayer.posZ, -maxMotion, maxMotion);
    }
    public static boolean hasMoved() {
        return mc.thePlayer != null && (mc.thePlayer.prevPosX != mc.thePlayer.posX
                || mc.thePlayer.prevPosY != mc.thePlayer.posY || mc.thePlayer.prevPosZ != mc.thePlayer.posZ);
    }
    public static boolean isInLiquid() {
        if (mc.thePlayer == null) return false;
        if (mc.thePlayer.isInWater()) return true;
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().contract(0.1000000014901161, 0.4000000059604645, 0.1000000014901161);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1); x++) {
            for (int y = MathHelper.floor_double(box.minY); y < MathHelper.floor_double(box.maxY + 1); y++) {
                for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1); z++) {
                    if (mc.thePlayer.worldObj.getBlockState(pos.set(x, y, z)).getBlock().getMaterial() == net.minecraft.block.material.Material.lava) return true;
                }
            }
        }
        return false;
    }
    public static boolean isJumping() {
        return mc.gameSettings.keyBindJump.isKeyDown() || (mc.thePlayer != null && mc.thePlayer.movementInput.jump);
    }
    public static boolean isSneaking() {
        return mc.gameSettings.keyBindSneak.isKeyDown() || (mc.thePlayer != null && mc.thePlayer.movementInput.sneak);
    }
    public static void setSpeed(boolean onlyWhenMoving, double speed, double yawRadians) {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.movementInput == null || !isMoving()) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            mc.thePlayer.motionX = -Math.sin(yawRadians) * speed;
            mc.thePlayer.motionZ = Math.cos(yawRadians) * speed;
        }
    }
    public static void addSpeed(double speed) {
        if (mc.thePlayer == null) return;
        float direction = (float) Math.toRadians(getDirection());
        if (isMoving()) {
            mc.thePlayer.motionX += -Math.sin(direction) * speed;
            mc.thePlayer.motionZ += Math.cos(direction) * speed;
        }
    }
    public static boolean isMovementRestricted(EntityLivingBase entity) {
        if (entity.isInWeb) return true;
        Block block = entity.worldObj.getBlockState(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ)).getBlock();
        if ((block == Blocks.ladder || block == Blocks.vine)
                && (entity instanceof com.adjustclient.util.SimulationPlayer || !entity.isSpectator())) return true;
        return entity.isInWater() || entity.isInLava();
    }
    public static float getDirection() {
        if (mc.thePlayer == null) return 0;
        if (TargetStrafeModule.INSTANCE.strafeState != null)
            return TargetStrafeModule.INSTANCE.strafeState.movementYaw;
        Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
        return getDirection(rotation == null ? mc.thePlayer.rotationYaw : rotation.x);
    }
    public static boolean isMoving() {
        return mc.thePlayer != null && mc.thePlayer.movementInput != null
                && (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0);
    }
}
