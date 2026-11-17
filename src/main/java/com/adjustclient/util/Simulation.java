package com.adjustclient.util;

import com.adjustclient.module.impl.movement.speed.SpeedModule;
import com.adjustclient.module.impl.movement.speed.SpeedState;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.util.rotation.RotationHolder;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import com.adjustclient.module.impl.movement.MovementFixModule;

public final class Simulation extends EntitySimulation implements RotationHolder {
    public SimulationPlayer player;
    public Object field80;
    public int field84;

    public Simulation() { }
    public Simulation(EntityPlayer source) { simulateTick(source, false); }

    public void setRotation(float[] input, boolean jump) {
        player.movementInput.moveForward = input[0];
        player.movementInput.moveStrafe = input[1];
        player.movementInput.jump = jump;
    }

    public Simulation includeSpeedModule() {
        SpeedModule speed = SpeedModule.INSTANCE;
        if (!speed.isEnabled()) return this;
        player.speedMode = speed.mode.getValue();
        SpeedState original = speed.state;
        if (original != null) {
            player.speedState = original.copy(player);
        }
        return this;
    }

    public void simulateTick(EntityPlayer source, boolean roundInput) {
        Minecraft mc = Minecraft.getMinecraft();
        if (player == null || player.worldObj != source.worldObj) {
            int nextId = Entity.nextEntityID;
            player = new SimulationPlayer(source.worldObj);
            Entity.nextEntityID = nextId;
        }
        SimulationPlayer player = this.player;
        player.posX = source.posX;
        player.posY = source.posY;
        player.posZ = source.posZ;
        player.prevPosX = source.prevPosX;
        player.prevPosY = source.prevPosY;
        player.prevPosZ = source.prevPosZ;
        player.lastTickPosX = source.lastTickPosX;
        player.lastTickPosY = source.lastTickPosY;
        player.lastTickPosZ = source.lastTickPosZ;
        player.setEntityBoundingBox(source.getEntityBoundingBox());
        player.inWater = source.inWater;
        player.motionX = source.motionX;
        player.motionY = source.motionY;
        player.motionZ = source.motionZ;
        player.isCollidedHorizontally = source.isCollidedHorizontally;
        player.isCollidedVertically = source.isCollidedVertically;
        player.isCollided = source.isCollided;
        player.rotationYaw = source.rotationYaw;
        player.rotationPitch = source.rotationPitch;
        if (source instanceof EntityPlayerSP && !MovementFixModule.INSTANCE.isEnabled()) {
            Vec2f rotation = RenderRotationTracker.getRotation();
            if (rotation != null) {
                player.rotationYaw = rotation.x;
                player.rotationPitch = rotation.y;
            } else {
                player.rotationYaw = mc.thePlayer.rotationYaw;
                player.rotationPitch = mc.thePlayer.rotationPitch;
            }
        }
        player.onGround = source.onGround;
        player.onGroundSpeedFactor = source.onGroundSpeedFactor;
        player.prevOnGroundSpeedFactor = source.prevOnGroundSpeedFactor;
        player.ticksExisted = source.ticksExisted;
        player.isDead = false;
        player.noClip = false;
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) player.removePotionEffectClient(effect.getPotionID());
        for (PotionEffect effect : source.getActivePotionEffects()) player.addPotionEffect(new PotionEffect(effect));
        MovementInput input = null;
        if (source instanceof SimulationPlayer) {
            player.setSprinting(source.isSprinting());
            SimulationPlayer other = (SimulationPlayer)source;
            player.sprintingTicksLeft = other.sprintingTicksLeft;
            player.sprintToggleTimer = other.sprintToggleTimer;
            player.sprintKeyDown = other.sprintKeyDown;
            input = other.movementInput;
        } else if (source instanceof EntityPlayerSP) {
            player.setSprinting(source.isSprinting());
            EntityPlayerSP other = (EntityPlayerSP)source;
            player.sprintingTicksLeft = other.sprintingTicksLeft;
            player.sprintToggleTimer = other.sprintToggleTimer;
            player.sprintKeyDown = source.isSprinting() || mc.gameSettings.keyBindSprint.isKeyDown();
            input = other.movementInput;
        }
        if (input != null) {
            player.movementInput.moveForward = roundInput ? (float)(input.moveForward > 0.0F ? MathHelper.ceiling_float_int(input.moveForward) : MathHelper.floor_float(input.moveForward)) : input.moveForward;
            player.movementInput.moveStrafe = roundInput ? (float)(input.moveStrafe > 0.0F ? MathHelper.ceiling_float_int(input.moveStrafe) : MathHelper.floor_float(input.moveStrafe)) : input.moveStrafe;
        }
        player.movementSpeed = source.getAIMoveSpeed();
        player.setAIMoveSpeed(player.movementSpeed);
        player.fallDistance = source.fallDistance;
        PlayerCapabilities capabilities = new PlayerCapabilities();
        capabilities.setFlySpeed(source.capabilities.getFlySpeed());
        capabilities.setPlayerWalkSpeed(source.capabilities.getWalkSpeed());
        capabilities.disableDamage = source.capabilities.disableDamage;
        capabilities.isFlying = source.capabilities.isFlying;
        capabilities.allowFlying = source.capabilities.allowFlying;
        capabilities.isCreativeMode = source.capabilities.isCreativeMode;
        capabilities.allowEdit = source.capabilities.allowEdit;
        player.capabilities = capabilities;
        player.offGroundTicks = source.offGroundTicks;
        player.onGroundTicks = source.onGroundTicks;
        player.jumpTicks = source.jumpTicks;
        player.moveForward = source.moveForward;
        player.moveStrafing = source.moveStrafing;
        player.field692 = null;
        player.lastDistance = source.lastDistance;
    }
}
