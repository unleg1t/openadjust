package com.adjustclient.util;

import com.adjustclient.event.SprintEvent;
import com.adjustclient.module.impl.movement.speed.SpeedMode;
import com.adjustclient.module.impl.movement.speed.SpeedState;
import com.adjustclient.util.rotation.SimulatedEntity;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public final class SimulationPlayer extends EntityPlayer implements SimulatedEntity {
    public static final GameProfile PROFILE;
    public static byte simulationModeState = 2;
    public MovementInput movementInput = new MovementInput();
    public net.minecraft.util.AxisAlignedBB field692;
    public SpeedMode speedMode;
    public Object speedState;
    public Consumer<com.adjustclient.event.MoveEvent> afterMovement;
    public float movementSpeed = Float.intBitsToFloat(1);
    public int sprintToggleTimer;
    public int sprintingTicksLeft;
    public boolean field720;
    public boolean sprintKeyDown;
    public Object field724;

    static { PROFILE = new GameProfile(net.minecraft.util.MathHelper.getRandomUuid(java.util.concurrent.ThreadLocalRandom.current()), "SimulationPlayer"); }

    public SimulationPlayer(World world) { super(world, PROFILE); }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        sprintingTicksLeft = sprinting ? 600 : 0;
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward) {
        super.moveEntityWithHeading(strafe, forward);
        if (afterMovement != null) afterMovement.accept(null);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void moveFlying(float strafe, float forward, float friction) {
        super.moveFlying(strafe, forward, friction);
        if (speedMode != null) {
            ((SpeedMode) speedMode).update((SpeedState) speedState);
        }
    }

    public boolean isSpectator() { return false; }

    public void suppressedPlayerTick() { }

    @Override
    public float getAIMoveSpeed() {
        return movementSpeed == Float.intBitsToFloat(1) ? super.getAIMoveSpeed() : movementSpeed;
    }

    @Override
    public void onLivingUpdate() {
        if (sprintingTicksLeft > 0 && --sprintingTicksLeft == 0) setSprinting(false);
        if (sprintToggleTimer > 0) --sprintToggleTimer;
        float oldForward = movementInput.moveForward;
        boolean usingItem = isUsingItem();
        if (usingItem && !isRiding()) {
            movementInput.moveStrafe *= 0.2F;
            movementInput.moveForward *= 0.2F;
            sprintToggleTimer = 0;
        }
        boolean sneaking = movementInput.sneak;
        double offset = width * 0.35F;
        double y = getEntityBoundingBox().minY + 0.5;
        pushOutOfBlocks(posX - offset, y, posZ + offset);
        pushOutOfBlocks(posX - offset, y, posZ - offset);
        pushOutOfBlocks(posX + offset, y, posZ - offset);
        pushOutOfBlocks(posX + offset, y, posZ + offset);
        boolean enoughFood = getFoodStats().getFoodLevel() > 6.0F || capabilities.allowFlying;
        field720 = false;
        if (onGround && !sneaking && oldForward < 0.8F && movementInput.moveForward >= 0.8F
                && !isSprinting() && enoughFood && !usingItem
                && (!SprintEvent.isSprintAllowed() || !isPotionActive(Potion.blindness))) {
            if (sprintToggleTimer <= 0 && !sprintKeyDown) sprintToggleTimer = 7;
            else setSprinting(true);
        }
        if (!isSprinting() && movementInput.moveForward >= 0.8F && enoughFood && !usingItem
                && (!SprintEvent.isSprintAllowed() || !isPotionActive(Potion.blindness)) && sprintKeyDown) setSprinting(true);
        if (isSprinting() && (movementInput.moveForward < 0.8F || isCollidedHorizontally || !enoughFood)) setSprinting(false);
        super.onLivingUpdate();
    }

    @Override
    protected void updateEntityActionState() {
        if (simulationModeState == 2) {
            super.updateEntityActionState();
            rotationYawHead = rotationYaw;
        }
        moveStrafing = movementInput.moveStrafe;
        moveForward = movementInput.moveForward;
        isJumping = movementInput.jump;
    }

    @Override
    public boolean isServerWorld() { return true; }

    @Override
    public void onItemUseFinish() { }

    @Override
    protected void resetHeight() { }

    @Override
    public void spawnRunningParticles() { }

    @Override
    public void playSound(String name, float volume, float pitch) { }

    @Override
    protected boolean canTriggerWalking() { return false; }

    public void tick() { ++ticksExisted; super.onUpdate(); }

    @Override
    public void onUpdate() { }
}
