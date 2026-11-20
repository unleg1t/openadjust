package com.adjustclient.modules.impl.combat.displacement;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.packet.PacketHold;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.rotation.InstantRotationModel;
import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.*;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class DisplacementModule extends Module {
    public static DisplacementModule INSTANCE;
    public final DisplacementSettings displacement;
    private final Int2IntOpenHashMap cooldowns = new Int2IntOpenHashMap();
    private PacketHold rotationHold;
    public int holdTicks;
    public boolean evaluated;
    public boolean displaced;
    private boolean resetClick;
    private boolean swapPending;
    public boolean swapped;

    public DisplacementModule() {
        super("Displacement", Category.COMBAT);
        INSTANCE = this;
        displacement = new DisplacementSettings(this);
    }

    public boolean tryDisplaceTarget() {
        if (!isEnabled()) return false;
        if (evaluated) return displaced;
        if (displacement.knockbackItem.isEnabled()
                && EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, mc.thePlayer.getHeldItem()) < 1
                && (!displacement.autoSwap.isEnabled() || WorldUtil.findKnockbackSlot() == null)) return false;
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return false;
        TrackedLivingEntity target = ServerTracker.INSTANCE.entityTracker.get(mc.objectMouseOver.entityHit.getEntityId(), TrackedLivingEntity.class);
        return target != null && displace(target);
    }

    @EventHandler(priority = 92) public void onTick(IngameActionEvent event) {
        if (event.suppressActions) return;
        if (!isEnabled()) { swapPending = false; return; }
        if (!swapPending) return;
        if (displacement.autoSwap.isEnabled()) {
            Integer slot = WorldUtil.findKnockbackSlot();
            if (slot != null) {
                Adjust.slotSpoofer.slot = slot;
                Adjust.slotSpoofer.active = true;
                Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
                Adjust.slotSpoofer.field17 = false;
                Adjust.slotSpoofer.renderSpoofed = false;
                Adjust.mouseClickHelper.left.clicks = 1;
                Adjust.mouseClickHelper.left.down = Boolean.TRUE;
                Adjust.mouseClickHelper.left.holdTicks = 0;
                swapped = true;
            }
        }
        swapPending = false;
    }

    @EventHandler(priority = 20)
    public void onAttackTick(AttackInputStageEvent event) {
        if (event.isCancelled()) return;
        var click = Adjust.mouseClickHelper.left;
        if (resetClick) {
            click.clicks = 1;
            click.down = Boolean.TRUE;
            resetClick = false;
        }
        if (click.disabled) return;
        if ((click.clicks != null ? click.clicks : click.key.pressTime) < 1) return;
        if (!isEnabled() || evaluated) return;
        if (displacement.knockbackItem.isEnabled()
                && EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, mc.thePlayer.getHeldItem()) < 1
                && (!displacement.autoSwap.isEnabled() || WorldUtil.findKnockbackSlot() == null)) return;
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null) return;
        TrackedLivingEntity target = ServerTracker.INSTANCE.entityTracker.get(mc.objectMouseOver.entityHit.getEntityId(), TrackedLivingEntity.class);
        if (target != null) displace(target);
    }

    public boolean displace(TrackedLivingEntity target) {
        Boolean cached = checkRequirements(target);
        if (cached != null) return cached;
        int mask = displacement.getTargetMask();
        if (!target.matchesType(mask)) return false;
        mask = displacement.getTargetMask();
        if (!target.matches(mask)) return false;
        float strength = WorldUtil.getKnockbackStrength(mc.thePlayer);
        if (strength == 0.0F) return false;
        evaluated = true;
        Float yaw = chooseDirection(target, strength);
        if (yaw == null) return false;
        RotationHandler.rotate(InstantRotationModel.INSTANCE, yaw, mc.thePlayer.rotationPitch);
        Adjust.mouseClickHelper.left.disabled = true;
        cooldowns.put(target.entityId, mc.thePlayer.ticksExisted + displacement.getCooldown());
        resetClick = true;
        displaced = true;
        swapPending = true;
        if (displacement.hideRotations.isEnabled()) { holdRotations(); holdTicks = 2; }
        return true;
    }

    private boolean isBetter(DisplacementResult candidate, DisplacementResult previous) {
        if ((candidate.voidTick() != Integer.MAX_VALUE) != (previous.voidTick() != Integer.MAX_VALUE))
            return candidate.voidTick() != Integer.MAX_VALUE;
        if (candidate.hitWall() != previous.hitWall()) return !candidate.hitWall();
        if (candidate.voidTick() == Integer.MAX_VALUE) {
            if (candidate.staysOverDrop() != previous.staysOverDrop()) return candidate.staysOverDrop();
            return previous.fallTick() > candidate.fallTick();
        }
        if (candidate.staysOverVoid() != previous.staysOverVoid()) return candidate.staysOverVoid();
        return previous.voidTick() > candidate.voidTick();
    }

    private Float chooseDirection(TrackedLivingEntity target, float strength) {
        var entity = target.getEntity();
        float towards = RotationUtil.getYawBetween(
                new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ), mc.thePlayer.getPositionEyes(1.0F)).x;
        float base = mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(towards - mc.thePlayer.rotationYaw);
        if (displacement.voidEnabled.isEnabled() && displacement.voidBind.getValue().isHeld() && target instanceof TrackedFakePlayer) {
            float[] offsets = {0, 90, -90, 105, -105, 75, -75, 120, -120, 60, -60, 135, -135, 45, -45, 150, -150, 30, -30, 165, -165, 15, -15, 180};
            DisplacementResult best = null;
            Float bestYaw = null;
            for (float offset : offsets) {
                float yaw = base + offset;
                DisplacementResult result = simulateKnockback((EntityPlayer) entity, yaw, strength);
                if (result.voidTick() == Integer.MAX_VALUE && result.fallTick() == Integer.MAX_VALUE) continue;
                if (best == null || isBetter(result, best)) { best = result; bestYaw = yaw; }
            }
            if (bestYaw != null) return bestYaw;
        }
        if (!displacement.landEnabled.isEnabled() || !displacement.landBind.getValue().isHeld() || !(target instanceof TrackedFakePlayer)) return null;
        double motionDegrees = Math.atan2(-entity.motionX, entity.motionZ) * 57.29577951308232D;
        if (motionDegrees < 0.0D) motionDegrees += 360.0D;
        float motionYaw = (float) motionDegrees;
        float approach = RotationUtil.getYawBetween(mc.thePlayer.getPositionEyes(1.0F), new Vec3(entity.posX, entity.posY, entity.posZ)).x;
        float difference = Math.abs(MathHelper.wrapAngleTo180_float(motionYaw - approach));
        if (displacement.targetFacingAway.isEnabled() && difference <= 90.0F) return null;
        return switch (displacement.landPreference.getValue()) {
            case LEFT -> base - 90.0F;
            case RIGHT -> base + 90.0F;
            case TOWARDS -> base + 180.0F;
            case RANDOM -> base + (RandomUtil.RANDOM.nextBoolean() ? 90.0F : -90.0F);
        };
    }

    private DisplacementResult simulateKnockback(EntityPlayer source, float yaw, float strength) {
        Simulation simulation = new Simulation(source);
        var player = simulation.player;
        float sin = MathHelper.sin(yaw * 0.017453292F), cos = MathHelper.cos(yaw * 0.017453292F);
        float dx = -sin * strength, dz = cos * strength;
        double initialX = mc.thePlayer.posX - player.posX, initialZ = mc.thePlayer.posZ - player.posZ;
        if (initialX * initialX + initialZ * initialZ < 0.0001) { initialX = sin; initialZ = -cos; }
        player.knockBack(mc.thePlayer, 0.0F, initialX, initialZ);
        player.motionX += dx;
        player.motionY += 0.1;
        player.motionZ += dz;
        player.isAirBorne = true;
        int voidTick = Integer.MAX_VALUE, fallTick = Integer.MAX_VALUE;
        boolean recoveredVoid = false, recoveredFall = false, hitWall = false;
        for (int tick = 1; tick < 21; ++tick) {
            player.tick();
            Vec3 feet = new Vec3(player.posX, player.posY, player.posZ);
            boolean voidBelow = WorldUtil.isFreeAt(feet, 0.0);
            if (voidBelow) { if (voidTick == Integer.MAX_VALUE) voidTick = tick; }
            else if (voidTick != Integer.MAX_VALUE) recoveredVoid = true;
            boolean dropBelow = WorldUtil.isFreeAt(feet, player.posY - 5.0);
            if (dropBelow) { if (fallTick == Integer.MAX_VALUE) fallTick = tick; }
            else if (fallTick != Integer.MAX_VALUE) recoveredFall = true;
            hitWall |= player.isCollidedHorizontally;
            if (tick == 10) {
                double x = mc.thePlayer.posX - player.posX, z = mc.thePlayer.posZ - player.posZ;
                if (x * x + z * z < 0.0001) { x = sin; z = -cos; }
                player.knockBack(mc.thePlayer, 0.0F, x, z);
                player.motionX += dx;
                player.motionY += 0.1;
                player.motionZ += dz;
                player.isAirBorne = true;
            }
        }
        return new DisplacementResult(voidTick, fallTick, hitWall, voidTick != Integer.MAX_VALUE && !recoveredVoid, fallTick != Integer.MAX_VALUE && !recoveredFall);
    }

    private Boolean checkRequirements(TrackedLivingEntity target) {
        if (!isEnabled()) return Boolean.FALSE;
        if (evaluated) return displaced;
        if (target != null && cooldowns.getOrDefault(target.entityId, 0) > mc.thePlayer.ticksExisted) return Boolean.FALSE;
        if (!displacement.knockbackItem.isEnabled()
                || EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, mc.thePlayer.getHeldItem()) >= 1
                || (displacement.autoSwap.isEnabled() && WorldUtil.findKnockbackSlot() != null)) return null;
        return Boolean.FALSE;
    }

    @EventHandler(priority = 1000)
    public void onPreClientTick(PreClientTickEvent event) {
        if (!isEnabled()) return;
        evaluated = false;
        displaced = false;
        swapped = false;
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (isEnabled()) cooldowns.clear();
    }

    @Override public void onDisable() {
        evaluated = false;
        displaced = false;
        swapped = false;
        cooldowns.clear();
        if (rotationHold != null) {
            Adjust.outboundPacketQueue.removeHold(rotationHold);
            rotationHold = null;
        }
    }
    @Override public String getDescription() { return "Changes the knockback direction of entities you hit."; }
    @Override public String getAdditionalDescription() { return "May not work very well when combined with Auto-Block."; }
    private void holdRotations() {
        if (rotationHold == null) rotationHold = Adjust.outboundPacketQueue.addHold(null, false);
    }
    public void releaseRotations() {
        if (rotationHold != null) { Adjust.outboundPacketQueue.removeHold(rotationHold); rotationHold = null; }
    }
}
