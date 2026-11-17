package com.adjustclient.module.impl.player.autopotion;

import com.adjustclient.Adjust;
import com.adjustclient.module.Module;
import com.adjustclient.module.Category;
import com.adjustclient.module.impl.combat.targetstrafe.TargetStrafeModule;
import com.adjustclient.module.impl.movement.antivoid.AntiVoidModule;
import com.adjustclient.event.*;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.simulation.PotionSimulation;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import com.adjustclient.game.player.InventoryStateTracker;

public final class AutoPotionModule extends Module {
    public static AutoPotionModule INSTANCE;
    public final PotionSettings potions;
    public final Int2ObjectOpenHashMap<Cooldown> cooldowns = new Int2ObjectOpenHashMap<>();
    public Float pitch;
    public PotionCandidate candidate;
    public ThrowPlan plan;
    public boolean usedPotion;
    public boolean standingThrow;

    public AutoPotionModule() {
        super("Auto Potion", Category.PLAYER);
        INSTANCE = this;
        potions = new PotionSettings(this);
    }

    public boolean tryStandingJump() {
        boolean unavailable = true;
        if (mc.thePlayer.onGround && mc.thePlayer.onGroundTicks >= 2 && mc.thePlayer.motionY < 0.0D
                && !mc.thePlayer.movementInput.jump) unavailable = WorldUtil.hasBlockBelowEyes();
        if (standingThrow) {
            standingThrow = false;
            if (unavailable) { pitch = null; candidate = null; return true; }
            if (mc.thePlayer.rotationPitch < -84.0F) {
                usePotion();
                mc.thePlayer.jump();
            }
            return true;
        }
        if (MovementUtil.isMoving() || MovementUtil.hasMoved() || unavailable) return false;
        float throwPitch = RandomUtil.RANDOM.nextBoolean() ? -90.0F : RandomUtil.nextFloat(-87.0F, -90.0F);
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        PotionSimulation potion = new PotionSimulation();
        potion.target = simulation.player;
        potion.setHeadingFromThrower(mc.theWorld, mc.thePlayer, mc.thePlayer.posX, mc.thePlayer.posY,
                mc.thePlayer.posZ, mc.thePlayer.rotationYaw, throwPitch);
        simulation.player.jump();
        boolean free = WorldUtil.isOverVoid();
        for (int tick = 0; tick < 7; tick++) {
            simulation.player.tick();
            if (!free && WorldUtil.isFreeAt(simulation.player.getPositionVector(), 0.0D)) break;
            if (simulation.player.isCollidedHorizontally) break;
            if (!potion.onUpdate()) continue;
            MovingObjectPosition hit = potion.collision;
            if (hit.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || hit.entityHit != simulation.player) return false;
            if (!potions.shouldThrow(candidate, simulation.player, 1.0D)) return false;
            RotationHandler.rotate(InstantRotationModel.INSTANCE, mc.thePlayer.rotationYaw, throwPitch);
            pitch = throwPitch;
            rememberInput(tick);
            standingThrow = true;
            return true;
        }
        return false;
    }

    public boolean isPotionOnCooldown(int potionId) {
        if (!cooldowns.containsKey(potionId)) return false;
        Cooldown cooldown = cooldowns.get(potionId);
        if (cooldown.durationMs < 0L) return true;
        if (cooldown.isExpired()) { cooldowns.remove(potionId); return false; }
        return true;
    }

    @EventHandler(priority = 31) public void onMotion(IngameActionEvent event) {
        if (!isEnabled()) return;
        if (plan != null && plan.ticks-- < 1) plan = null;
        if (usedPotion) {
            usedPotion = false;
            if (Adjust.slotSpoofer.active) {
                Adjust.mouseClickHelper.right.disabled = true;
                return;
            }
        }
        if (event.suppressActions || cannotThrow() || mc.thePlayer.isUsingItem()) {
            plan = null; pitch = null; standingThrow = false; candidate = null;
            return;
        }
        if (candidate == null) {
            candidate = potions.findCandidate();
            if (candidate == null) return;
        }
        if (potions.standingJump.isEnabled() && tryStandingJump()) return;
        if (potions.moving.isEnabled() && MovementUtil.hasMoved() && mc.timer.timerSpeed == 1.0F && tryMoving()) return;
        if (potions.ground.isEnabled() && tryGround()) return;
        pitch = null; standingThrow = false; candidate = null;
    }

    @EventHandler(priority = -1) public void onJump(SprintEligibilityEvent event) {
        if (!isEnabled() || plan == null || !plan.sprinting) return;
        mc.gameSettings.keyBindSprint.pressed = true;
        mc.thePlayer.omniSprint = true;
        event.sprintEligible = true;
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if (!isEnabled() || plan == null || InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory) return;
        event.forward = (plan.keys & 1) != 0;
        event.back = (plan.keys & 2) != 0;
        event.left = (plan.keys & 4) != 0;
        event.right = (plan.keys & 8) != 0;
        event.jump = (plan.keys & 16) != 0;
        event.sneak = (plan.keys & 32) != 0;
    }

    public void rememberInput(int ticks) {
        if (plan != null) { plan.ticks = ticks; return; }
        if (TargetStrafeModule.INSTANCE.strafeState == null) {
            Vec2f rendered = com.adjustclient.rotation.handler.RenderRotationTracker.INSTANCE.rotation;
            MovementUtil.getDirection(rendered == null ? mc.thePlayer.rotationYaw : rendered.x);
        }
        int keys = (mc.thePlayer.movementInput.moveForward > 0 ? 1 : 0)
                | (mc.thePlayer.movementInput.moveForward < 0 ? 2 : 0)
                | (mc.thePlayer.movementInput.moveStrafe > 0 ? 4 : 0)
                | (mc.thePlayer.movementInput.moveStrafe < 0 ? 8 : 0)
                | (mc.thePlayer.movementInput.jump ? 16 : 0) | (mc.thePlayer.movementInput.sneak ? 32 : 0);
        plan = new ThrowPlan(keys, ticks, (mc.thePlayer.getDataWatcher().getWatchableObjectByte(0) & 8) != 0);
    }

    @Override public void onDisable() {
        cooldowns.clear(); plan = null; usedPotion = false; pitch = null; standingThrow = false; candidate = null;
    }

    public static boolean cannotThrow() {
        if (!mc.thePlayer.addedToChunk) return true;
        if (mc.playerController.getCurrentGameType() != net.minecraft.world.WorldSettings.GameType.SURVIVAL
                && mc.playerController.getCurrentGameType() != net.minecraft.world.WorldSettings.GameType.ADVENTURE) return true;
        if (InventoryStateTracker.isInServersideGui()
                || com.adjustclient.module.impl.movement.scaffold.ScaffoldModule.INSTANCE.isEnabled()
                || TargetStrafeModule.INSTANCE.strafeState != null) return true;
        ServerTracker tracker = ServerTracker.INSTANCE;
        if (tracker.packetTimer.hasElapsed(200L)) return true;
        int delay = tracker.serverState == null ? 0 : tracker.serverState.packetWatcher.getAverageDelay();
        if (!tracker.velocityTimer.hasElapsed(delay + 300L)) return true;
        if (AntiVoidModule.INSTANCE.blink.blinkState != null) return true;
        return mc.thePlayer.ticksExisted < 25;
    }

    public boolean tryGround() {
        if (WorldUtil.isOverVoid()) return false;
        if (pitch != null) {
            if (mc.thePlayer.rotationPitch > 86.0F) usePotion();
            if (pitch == null) return false;
            RotationHandler.INSTANCE.target = new Vec2f(mc.thePlayer.rotationYaw, pitch);
            RotationHandler.INSTANCE.model = InstantRotationModel.INSTANCE;
            RotationHandler.INSTANCE.requested = true; RotationHandler.INSTANCE.active = true;
            RotationHandler.release();
            return true;
        }
        Vec2f rotation = ServerTracker.INSTANCE.rotation;
        if (rotation == null) return false;
        float throwPitch = RandomUtil.RANDOM.nextBoolean() ? 90.0F : RandomUtil.nextFloat(87.0F, 90.0F);
        if (!mc.thePlayer.movementInput.sneak) {
            MovingObjectPosition hit = WorldUtil.rayTracePlayerBlocks(rotation.x, throwPitch,
                    mc.playerController.isInCreativeMode() ? 5.0D : 4.5D);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && BlockUtil.isInteractable(mc.theWorld.getBlockState(hit.getBlockPos()).getBlock())) return false;
        }
        PotionSimulation potion = new PotionSimulation();
        potion.setHeadingFromThrower(mc.theWorld, mc.thePlayer, mc.thePlayer.posX, mc.thePlayer.posY,
                mc.thePlayer.posZ, rotation.x, throwPitch);
        for (int tick = 0; tick < 7; tick++) {
            if (!potion.onUpdate()) continue;
            MovingObjectPosition hit = potion.collision;
            double multiplier;
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && hit.entityHit == mc.thePlayer) multiplier = 1.0D;
            else if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                double distance = mc.thePlayer.getDistanceSq(potion.posX, potion.posY, potion.posZ);
                if (distance >= 16.0D) continue;
                multiplier = 1.0D - Math.sqrt(distance) / 4.0D;
                if (multiplier <= 0.85D) continue;
            } else continue;
            if (!potions.shouldThrow(candidate, mc.thePlayer, multiplier)) break;
            rememberInput(tick);
            pitch = throwPitch;
            return true;
        }
        return pitch != null;
    }

    public boolean tryMoving() {
        MovingObjectPosition mouseOver = mc.objectMouseOver;
        if (!mc.thePlayer.movementInput.sneak && mouseOver != null
                && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && BlockUtil.isInteractable(mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock())) return false;
        Vec2f rotation = ServerTracker.INSTANCE.rotation;
        if (rotation == null) return false;
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        PotionSimulation potion = new PotionSimulation();
        potion.target = simulation.player;
        potion.setHeadingFromThrower(mc.theWorld, mc.thePlayer, mc.thePlayer.posX, mc.thePlayer.posY,
                mc.thePlayer.posZ, rotation.x, 0.0F);
        boolean free = WorldUtil.isOverVoid();
        for (int tick = 0; tick < 7; tick++) {
            simulation.player.tick();
            if (!free && WorldUtil.isColumnCollisionFree(simulation.player.getPositionVector(), 0.0D, mc.thePlayer.width * 0.5F)) return false;
            if (simulation.player.isCollidedHorizontally) return false;
            if (!potion.onUpdate()) continue;
            MovingObjectPosition hit = potion.collision;
            if (hit.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || hit.entityHit != simulation.player
                    || !potions.shouldThrow(candidate, simulation.player, 1.0D)) return false;
            rememberInput(tick);
            usePotion();
            return true;
        }
        return false;
    }

    public void usePotion() {
        Adjust.slotSpoofer.slot = candidate.slot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = false;
        Adjust.slotSpoofer.renderSpoofed = false;
        Adjust.mouseClickHelper.right.clicks = Integer.valueOf(1);
        Adjust.mouseClickHelper.right.down = Boolean.TRUE;
        Adjust.mouseClickHelper.right.holdTicks = 0;
        Adjust.mouseClickHelper.left.disabled = true;
        cooldowns.put(candidate.potionId, new Cooldown(2000L));
        usedPotion = true; pitch = null; standingThrow = false; candidate = null;
    }

    @EventHandler public void onPacket(LowLevelPacketReceiveEvent event) {
        if (!isEnabled()) return;
        Packet<?> packet = event.packet;
        if (packet instanceof S01PacketJoinGame || packet instanceof S07PacketRespawn) {
            cooldowns.clear(); plan = null;
            mc.addScheduledTask(() -> {
                pitch = null; standingThrow = false; candidate = null;
            });
        } else if (packet instanceof S08PacketPlayerPosLook) {
            mc.addScheduledTask(() -> {
                pitch = null; standingThrow = false; candidate = null;
            });
        } else if (packet instanceof S1DPacketEntityEffect) {
            S1DPacketEntityEffect effect = (S1DPacketEntityEffect) packet;
            mc.addScheduledTask(() -> {
                if (mc.thePlayer == null || cooldowns.isEmpty() || effect.getEntityId() != mc.thePlayer.getEntityId()) return;
                cooldowns.remove((int) effect.getEffectId());
                if (cooldowns.isEmpty()) plan = null;
            });
        } else if (packet instanceof S13PacketDestroyEntities && mc.theWorld != null && !cooldowns.isEmpty()) {
            for (int id : ((S13PacketDestroyEntities) packet).getEntityIDs()) {
                Entity entity = mc.theWorld.getEntityByID(id);
                if (!(entity instanceof EntityPotion)) continue;
                java.util.List<PotionEffect> effects = PotionHelper.getPotionEffects(((EntityPotion) entity).getPotionDamage(), false);
                if (effects == null) continue;
                for (PotionEffect effect : effects) {
                    if (effect.getPotionID() == Potion.heal.getId() && entity.getDistanceSqToEntity(mc.thePlayer) < 16.0D) {
                        cooldowns.remove(effect.getPotionID());
                        if (cooldowns.isEmpty()) plan = null;
                    }
                }
            }
        }
    }

    public static final class ThrowPlan {
        public final int keys;
        public int ticks;
        public final boolean sprinting;
        ThrowPlan(int keys, int ticks, boolean sprinting) { this.keys = keys; this.ticks = ticks; this.sprinting = sprinting; }
    }
}
