package com.adjustclient.module.impl.combat.autoblock;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.combat.autoblock.mode.*;
import com.adjustclient.setting.*;
import com.adjustclient.server.ServerType;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.*;
import com.adjustclient.module.impl.movement.antivoid.AntiVoidModule;
import com.adjustclient.game.player.InventoryStateTracker;

public final class AutoBlockModule extends Module {
    public static AutoBlockModule INSTANCE;
    public final ModeSetting<AutoBlockMode> mode;
    public final NumberSetting range;
    public final BooleanSetting targetsPlayers;
    public final BooleanSetting targetsHostile;
    public final BooleanSetting targetsTeammates;
    public final BooleanSetting targetsInvisibles;
    public final BooleanSetting projectilesFireballs;
    public final BooleanSetting projectilesArrows;
    public final BooleanSetting projectilesEnderPearls;

    public AutoBlockModule() {
        super("Auto Block", Category.COMBAT);
        INSTANCE = this;
        range = new NumberSetting(this, "Range", 6.0, 3.0, 12.0, 0.1);
        range.formatter(NumberSetting.METERS);
        FullAutoBlockMode full = new FullAutoBlockMode(this);
        PredictiveAutoBlockMode predictive = new PredictiveAutoBlockMode(this);
        mode = new ModeSetting<>(this, "Mode", full, full, predictive);
        mode.addListener((oldMode, newMode) -> {
            if (!isEnabled() || oldMode == newMode) return;
            if (oldMode instanceof PredictiveAutoBlockMode) {
                var old = (PredictiveAutoBlockMode) oldMode;
                old.resetAndFlush();
            }
            if (newMode instanceof PredictiveAutoBlockMode) {
                var next = (PredictiveAutoBlockMode) newMode;
                next.resetAndFlush();
            }
        });
        ExpandSetting targets = new ExpandSetting(this, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true);
        targetsHostile = new BooleanSetting(targets, "Hostile", false);
        targetsTeammates = new BooleanSetting(targets, "Teammates", false);
        targetsInvisibles = new BooleanSetting(targets, "Invisibles", true);
        ExpandSetting projectiles = new ExpandSetting(this, "Projectiles");
        projectilesFireballs = new BooleanSetting(projectiles, "Fireballs", true);
        projectilesArrows = new BooleanSetting(projectiles, "Arrows", true);
        projectilesEnderPearls = new BooleanSetting(projectiles, "Ender Pearls", true);
        addSetting(mode, 0);
        addSetting(targets, 1);
        addSetting(projectiles, 2);
        addSetting(range, 3);
    }

    public boolean hasSword(boolean useRenderSpoof) {
        var inventory = mc.thePlayer.inventory;
        ItemStack stack;
        if (useRenderSpoof && Adjust.slotSpoofer.field17) {
            if (!Adjust.slotSpoofer.active || Adjust.slotSpoofer.renderSpoofed) stack = inventory.getCurrentItem();
            else {
                int slot = inventory.currentItem;
                stack = slot >= 0 && slot < 9 ? inventory.mainInventory[slot] : null;
            }
        } else {
            int slot = Adjust.slotSpoofer.resolveSlot(inventory.currentItem);
            stack = slot >= 0 && slot < 9 ? inventory.mainInventory[slot] : null;
        }
        return stack != null && stack.getItem().getClass() == ItemSword.class;
    }
    @Override public void onEnable() { mode.getValue(); }
    @Override public void onDisable() {
        AutoBlockMode mode = this.mode.getValue();
        if (mode instanceof PredictiveAutoBlockMode) {
            var predictive = (PredictiveAutoBlockMode) mode;
            predictive.resetAndFlush();
        }
    }
    @Override public String getSuffix() { return mode.getValue().getName(); }

    public boolean shouldBlock() {
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory) return false;
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit != null) {
            if (mc.playerController.getIsHittingBlock()) return false;
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                    && hit.entityHit != null && hit.entityHit.getClass() == EntityVillager.class) return false;
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !mc.thePlayer.movementInput.sneak
                    && BlockUtil.isInteractable(mc.theWorld.getBlockState(hit.getBlockPos()).getBlock())) return false;
        }
        if (AntiVoidModule.INSTANCE.blink.blinkState != null) return false;
        return !WorldUtil.isLookingAtBlock(false) && canBlockOnServer() && hasSword(true) && hasThreat();
    }

    public static boolean canBlockOnServer() {
        var server = ServerTracker.INSTANCE.serverState;
        if (server == null || server.antiCheatModel == null
                || server.antiCheatModel.serverType != ServerType.HYPIXEL) return true;
        var game = Adjust.gameStateTracker.state;
        if (game.isSpecialState() != 0) return false;
        return game.startTimer != null && !game.ended || game.game == HypixelGame.DUELS
                || game.game == HypixelGame.THE_HYPIXEL_PIT;
    }

    public boolean hasThreat() {
        var tracker = ServerTracker.INSTANCE.entityTracker;
        Vec3 feet = mc.thePlayer.getPositionVector();
        double squaredRange = range.getDouble() * range.getDouble();
        for (TrackedLivingEntity tracked : tracker.getEntities(targetMask(), TrackedLivingEntity.class)) {
            var target = tracked.getEntity();
            if (target.ticksExisted < 3) continue;
            Vec3 nearest = DistanceUtil.closestPoint(feet, target.getEntityBoundingBox());
            double distance = feet.squareDistanceTo(nearest);
            float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
            if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
            Vec3 eyes = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
            Vec3 targetEyes = target.getPositionVector().addVector(0.0, target.getEyeHeight(), 0.0);
            MovingObjectPosition collision = target.worldObj.rayTraceBlocks(eyes, targetEyes, false, false, false);
            if (collision == null) {
                if (distance <= squaredRange) return true;
            } else if (distance <= squaredRange * 0.5) return true;
        }
        for (TrackedEntity<?> tracked : tracker.getEntities(256, null)) {
            if (tracked == null) continue;
            if (tracked instanceof TrackedFireball && projectilesFireballs.isEnabled()) {
                var fireball = (TrackedFireball) tracked;
                if (fireball.isDangerous() && fireball.getImpactDistanceFactor() <= 0.80000001F) return true;
                continue;
            }
            if (tracked instanceof TrackedOtherEntity && projectilesArrows.isEnabled()) {
                EntityArrow arrow = (EntityArrow) tracked.getEntity();
                if (arrow.inGround) continue;
                double motionYaw = Math.toDegrees(Math.atan2(-arrow.motionX, arrow.motionZ));
                if (motionYaw < 0) motionYaw += 360.0;
                float towardArrow = RotationUtil.getYawBetween(mc.thePlayer.getPositionEyes(1.0F), arrow.getPositionVector()).x;
                double delta = Math.abs(MathHelper.wrapAngleTo180_double(motionYaw - towardArrow));
                if (Math.hypot(arrow.motionX, arrow.motionZ) > 0.2 || arrow.motionY >= 0.0) {
                    float angle = !MovementUtil.isMoving() && MovementUtil.getSpeed() <= 0.08 ? 25.0F : 55.0F;
                    if (delta > angle) return false;
                } else if ((float) Math.hypot(arrow.posX - mc.thePlayer.posX, arrow.posZ - mc.thePlayer.posZ) > 2.0F) return false;
                int latency = 0;
                var server = ServerTracker.INSTANCE.serverState;
                if (server != null && server.packetWatcher.delayCount != 0)
                    latency = server.packetWatcher.totalDelay / server.packetWatcher.delayCount;
                double distance = latency >= 131 ? 20.5 : 18.5;
                return mc.thePlayer.getDistanceSqToEntity(arrow) <= distance * distance;
            }
            if (tracked instanceof TrackedThrowable && projectilesEnderPearls.isEnabled()
                    && tracked.getEntity() instanceof EntityEnderPearl) {
                var pearl = (TrackedThrowable) tracked;
                if (!pearl.isDangerous()) continue;
                var impact = pearl.impact;
                if (impact.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    if (impact.entityHit == mc.thePlayer || feet.squareDistanceTo(
                            DistanceUtil.closestPoint(feet, impact.entityHit.getEntityBoundingBox())) <= squaredRange) return true;
                } else if (impact.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    var pos = impact.getBlockPos();
                    var box = mc.theWorld.getBlockState(pos).getBlock().getSelectedBoundingBox(mc.theWorld, pos);
                    if (feet.squareDistanceTo(DistanceUtil.closestPoint(feet, box)) <= squaredRange) return true;
                }
            }
        }
        return false;
    }

    public int targetMask() {
        return 0x400 | (targetsPlayers.isEnabled() ? 2 : 0) | (targetsHostile.isEnabled() ? 4 : 0)
                | (targetsTeammates.isEnabled() ? 0x20 : 0) | (targetsInvisibles.isEnabled() ? 0x40 : 0);
    }
}
