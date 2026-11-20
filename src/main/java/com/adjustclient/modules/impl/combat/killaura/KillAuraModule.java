package com.adjustclient.modules.impl.combat.killaura;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.entity.tracker.ServerPositionEntry;
import com.adjustclient.utilities.hypixel.HypixelGame;
import com.adjustclient.modules.impl.combat.autoblock.AutoBlockModule;
import com.adjustclient.modules.impl.combat.targetstrafe.TargetStrafeModule;
import com.adjustclient.modules.impl.exploit.murdermystery.MurderMysteryModule;
import com.adjustclient.modules.impl.movement.noslowdown.NoSlowdownModule;
import com.adjustclient.modules.impl.movement.speed.SpeedModule;
import com.adjustclient.modules.impl.combat.velocity.VelocityModule;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.modules.impl.combat.backtrack.BackTrackModule;
import com.adjustclient.modules.impl.combat.displacement.DisplacementModule;
import com.adjustclient.modules.impl.combat.killaura.target.*;
import com.adjustclient.modules.impl.movement.sprint.HypixelKeepSprintMode;
import com.adjustclient.modules.util.CpsCounter;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.managers.util.ServiceUtil;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.*;
import com.adjustclient.utilities.rotation.AimPoint;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;
import com.adjustclient.modules.impl.movement.sprint.SprintModule;

public final class KillAuraModule extends Module {
    public static KillAuraModule INSTANCE;
    public final KillAuraSettings settings;
    public TargetSelector targetSelector;
    public final com.adjustclient.utilities.Cooldown swingWindow;
    public final com.adjustclient.utilities.Cooldown swingDelay;
    public final FloatArrayList yawSamples;
    public final AimPointInterpolator aimInterpolator;
    public boolean active;

    public KillAuraModule() {
        super("Kill Aura", Category.COMBAT);
        INSTANCE = this;
        settings = new KillAuraSettings(this);
        swingWindow = new Cooldown(-1L);
        swingDelay = new Cooldown(-1L);
        yawSamples = new FloatArrayList();
        aimInterpolator = new AimPointInterpolator();
        notifyToggle = false;
    }

    @EventHandler(priority = 51) public void onMouseOver(IngameActionEvent event) {
        if (!isEnabled() || event.suppressActions || !active) return;
        TargetInfo target = targetSelector.rotationTarget;
        if (target == null || target.entity.isDead()) return;
        AimPoint aim = target.aimPoint;
        targetSelector.oldestTarget = targetSelector.previousPreviousTarget;
        targetSelector.previousPreviousTarget = targetSelector.previousTarget;
        targetSelector.previousTarget = target;
        if (settings.rotationsEnabled.isEnabled() && settings.rotationsVisibility.getValue() == RotationVisibility.SILENT) {
            Vec2f angles = aim.angles();
            RotationHandler.rotate(settings.rotationModel.createModel(), angles.x, angles.y);
        }
        if (target == targetSelector.attackTarget && isLookingAtTarget(target)) {
            WorldUtil.setMouseOver(new MovingObjectPosition(target.entity.getEntity(), aim.hitVec()));
        } else if (Adjust.gameStateTracker.state.game == HypixelGame.HOUSING
                && mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityArmorStand) {
            WorldUtil.clearMouseOver();
        }
    }

    public static boolean shouldDeferToKeepSprint() {
        if (BuilderModule.INSTANCE.blockIn.active) return false;
        var sprint = SprintModule.INSTANCE;
        if (!sprint.isEnabled() || !sprint.keepSprintEnabled.isEnabled()) return false;
        var mode = sprint.keepSprintMode.getValue();
        return mode instanceof HypixelKeepSprintMode && !((HypixelKeepSprintMode) mode).canAttack();
    }

    @EventHandler(priority = 60)
    public void onAttackTick(AttackInputStageEvent event) {
        if (event.isCancelled() || !isEnabled() || !active) return;
        var left = Adjust.mouseClickHelper.left;
        if (left.disabled) return;
        TargetInfo target = targetSelector.attackTarget;
        if (target != null && !target.entity.isDead()) {
            if (shouldDeferToKeepSprint()) return;
            if (isLookingAtTarget(target) && attack(target, false)) {
                if (DisplacementModule.INSTANCE.displace(target.entity)) return;
                RotationUtil.forceMouseOverEntity(target.entity.getEntity());
                targetSelector.recordAttack(target);
                left.clicks = 1;
                left.down = Boolean.TRUE;
                left.holdTicks = 0;
                left.swing = true;
                Adjust.cpsCounter.recordClick();
                long windowDuration = RandomUtil.nextInt(200) + 350L;
                swingWindow.timer.reset();
                swingWindow.durationMs = windowDuration;
                long delayDuration = RandomUtil.nextInt(100) + 50L;
                swingDelay.timer.reset();
                swingDelay.durationMs = delayDuration;
            }
            return;
        }
        if (targetSelector.nearestDistance <= settings.getSwingRange()) {
            long windowDuration = RandomUtil.nextInt(200) + 200L;
            swingWindow.timer.reset();
            swingWindow.durationMs = windowDuration;
            if (swingDelay.durationMs < 0) {
                long delayDuration = RandomUtil.nextInt(40) + 20L;
                swingDelay.timer.reset();
                swingDelay.durationMs = delayDuration;
            }
        }
        if (!canSwing()) return;
        int holdTicks = RandomUtil.nextInt(RandomUtil.RANDOM, 0, 3);
        left.clicks = 1;
        left.down = Boolean.TRUE;
        left.holdTicks = holdTicks;
        if (settings.rangeSwingHideSwings.isEnabled()) left.swing = mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
        long delayDuration = RandomUtil.nextInt(150) + 40L;
        swingDelay.timer.reset();
        swingDelay.durationMs = delayDuration;
    }

    public boolean canSwing() {
        return swingWindow.durationMs >= 0 && !swingWindow.timer.hasElapsed(swingWindow.durationMs)
                && !ServerTracker.INSTANCE.field199 && swingDelay.isExpired();
    }

    public boolean canAttack(boolean checkSettings) {
        if (mc.thePlayer == null) return false;
        if (checkSettings) {
            if (settings.holdAttack.isEnabled() && !Mouse.isButtonDown(0)) return false;
            if (settings.disableOnNoWeapon.isEnabled() && !WorldUtil.isHoldingWeapon()) return false;
            if (settings.disableOnBreakingBlock.isEnabled() && WorldUtil.isLookingAtBlock(true)) return false;
        }
        return GameStateUtil.isActiveUnlessScaffolding();
    }

    @EventHandler public void onMouseInput(MouseDeltaEvent event) {
        if (!isEnabled()) return;
        if (event.silent || !active || !settings.rotationsEnabled.isEnabled()
                || settings.rotationsVisibility.getValue() == RotationVisibility.SILENT) return;
        TargetInfo target = targetSelector.rotationTarget;
        if (target == null || target.entity.isDead()) return;
        EntityLivingBase entity = target.entity.getEntity();
        float partialTicks = event.partialTicks;
        Vec3 position = getInterpolatedPosition(entity, partialTicks);
        Vec3 eyes = mc.thePlayer.getPositionEyes(partialTicks);
        AimPoint point = target.aimPoint;
        Vec3 aim;
        if (!point.directAim()) aim = aimInterpolator.interpolate(entity, point, position, partialTicks);
        else {
            aim = RotationUtil.getAimVec(entity, position, eyes, settings.rotationsMultipointPreferCenterMass.isEnabled(),
                    settings.getMultipointHorizontal() / 100.0D, settings.getMultipointVertical() / 100.0D);
            aimInterpolator.sample = null;
            aimInterpolator.previousOffset = null;
            aimInterpolator.offset = null;
        }
        Vec2f previous = RenderRotationTracker.INSTANCE.rotation;
        if (previous == null) return;
        Vec2f rotation = RotationUtil.quantizeToMouseSensitivity(RotationUtil.getYawBetween(aim, eyes), previous);
        float yawDelta = MathHelper.wrapAngleTo180_float(rotation.x - previous.x);
        float pitchDelta = MathHelper.wrapAngleTo180_float(rotation.y - previous.y);
        if (settings.rotationsVisibility.getValue() == RotationVisibility.LINEAR) {
            float horizontal = settings.getHorizontalSpeed() / 100.0F;
            yawDelta *= horizontal;
            float vertical = settings.getVerticalSpeed() / 100.0F;
            pitchDelta *= vertical;
        }
        float sensitivity = event.sensitivity;
        float deltaY = (float) (pitchDelta / 0.15D / sensitivity);
        if (!mc.gameSettings.invertMouse) deltaY *= -1.0F;
        float deltaX = (float) (yawDelta / 0.15D / sensitivity);
        if (settings.rotationsVisibility.getValue() == RotationVisibility.LINEAR) {
            deltaX += event.deltaX;
            deltaY += event.deltaY;
        }
        event.deltaX = deltaX;
        event.deltaY = deltaY;
        event.silent = true;
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        while (yawSamples.size() >= 2) yawSamples.removeFloat(0);
        yawSamples.add(event.yaw);
    }

    @EventHandler public void onTargetTick(UserInputVerificationEvent event) {
        if (!isEnabled()) return;
        active = mc.thePlayer != null;
        if (active && settings.holdAttack.isEnabled() && !Mouse.isButtonDown(0)) active = false;
        if (active && settings.disableOnNoWeapon.isEnabled() && !WorldUtil.isHoldingWeapon()) active = false;
        if (active && settings.disableOnBreakingBlock.isEnabled() && WorldUtil.isLookingAtBlock(true)) active = false;
        if (active && ScaffoldModule.INSTANCE.isEnabled() && Boolean.TRUE.equals(ScaffoldModule.INSTANCE.placeReady)) active = false;
        if (active) active = GameStateUtil.isActive();
        if (!active) { targetSelector.attackTarget = null; targetSelector.rotationTarget = null; return; }
        targetSelector.update(yawSamples);
        if (targetSelector.attackTarget != null) event.inputRequested = true;
    }

    public boolean attack(TargetInfo target, boolean keepSprint) {
        int lead = keepSprint ? 1 : 0;
        if (!(settings.cps.min.getValue().longValue() == 1 && settings.cps.max.getValue().longValue() == 1)
                && Adjust.cpsCounter.canClick(settings.cps.getClickDelay(!keepSprint), CpsCounter.Button.LEFT, 50 * lead)) return true;
        if (target.extraAttackTimer != null && !target.extraAttackTimer.hasElapsed(250L)) return false;
        CombatStats stats = target.entity.combatStats;
        boolean ready = stats.getHitsToKill(lead);
        if (!ready && mc.thePlayer != null) ready = DamageUtil.computeDamage(target.entity.getEntity(), mc.thePlayer,
                mc.thePlayer.inventory.getCurrentItem(), false).damage > stats.lastDamage;
        if (!ready) return false;
        if (!keepSprint) target.extraAttackTimer = new Timer();
        return true;
    }

    public Vec3 getInterpolatedPosition(Entity entity, float partialTicks) {
        ServerPositionEntry entry = null;
        if (BackTrackModule.INSTANCE.target == null && entity.ridingEntity == null)
            entry = Adjust.entityTrackerService.tracker.entries.get(entity.getEntityId());
        if (entry == null) return new Vec3(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
        return new Vec3(entry.previousX + (entry.x - entry.previousX) * partialTicks,
                entry.previousY + (entry.y - entry.previousY) * partialTicks,
                entry.previousZ + (entry.z - entry.previousZ) * partialTicks);
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;
        float partialTicks = event.partialTicks;
        var manager = mc.getRenderManager();
        if (settings.renderRangeEnabled.isEnabled()) {
            RenderUtil.j();
            GlStateManager.disableDepth();
            RenderUtil.setupOverlayRendering(partialTicks);
            double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks - manager.viewerPosX;
            double y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks - manager.viewerPosY + 0.00625D;
            double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks - manager.viewerPosZ;
            double range = settings.getPlayerAttackRange();
            ServiceUtil.isPlayerStateRestricted();
            RenderUtil.drawCircle3d(settings.getRangeColor(), settings.renderRangeSecondaryColor.getValue().getRGB(), x, y, z, range + 0.1000000014901161D, 2.0F);
            GlStateManager.enableDepth();
            RenderUtil.k();
        }
        TargetInfo target = targetSelector.attackTarget != null ? targetSelector.attackTarget : targetSelector.rotationTarget;
        if (target == null || !settings.renderTargetEnabled.isEnabled()) return;
        EntityLivingBase entity = target.entity.getEntity();
        if (entity == null) return;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - manager.viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - manager.viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - manager.viewerPosZ;
        AxisAlignedBB box = RenderUtil.entityBoxAt(entity, x, y, z, settings.getTargetExpand());
        int color = entity.hurtTime < 1 ? settings.getTargetColor() : settings.getTargetHurtColor();
        RenderUtil.begin3d();
        RenderUtil.glColor(color);
        RenderUtil.drawFilledBox(box);
        if (settings.renderTargetOutline.isEnabled()) {
            GL11.glLineWidth(1.0F);
            RenderUtil.glColor((Math.min(255, (color >>> 24) + 30) << 24) | (color & 0xFFFFFF));
            RenderUtil.drawOutlinedBox(box);
        }
        RenderUtil.end3d();
    }

    public boolean isLookingAtTarget(TargetInfo target) {
        LookForMode mode = settings.rotationsLookFor.getValue();
        if (mode == LookForMode.DISABLED || !settings.rotationsEnabled.isEnabled()) return true;
        EntityLivingBase entity = target.entity.getEntity();
        AxisAlignedBB box = entity.getEntityBoundingBox();
        double expand = 0.0D;
        if (!target.attackTimer.hasElapsed(1000L) && targetSelector.lastAttacked == target && targetSelector.oldestTarget == target)
            expand = 0.1F;
        double horizontal = (box.maxX - box.minX) * expand;
        double vertical = (box.maxY - box.minY) * expand;
        box = box.expand(horizontal, vertical, horizontal);
        float yaw = mode == LookForMode.ONE_TICK || ServerTracker.INSTANCE.swung ? mc.thePlayer.lastReportedYaw : mc.thePlayer.rotationYaw;
        float pitch = mode == LookForMode.ONE_TICK || ServerTracker.INSTANCE.swung ? mc.thePlayer.lastReportedPitch : mc.thePlayer.rotationPitch;
        float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float cosPitch = -MathHelper.cos(-pitch * 0.017453292F);
        float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
        Vec3 direction = new Vec3(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0F);
        double range = settings.getAttackRange(entity);
        Vec3 end = eyes.addVector(direction.xCoord * range, direction.yCoord * range, direction.zCoord * range);
        return box.calculateIntercept(eyes, end) != null;
    }

    public boolean warnReplacedByAutoStrike(boolean enabling) {
        if (!enabling) return true;
        if (mc.thePlayer == null) return false;
        if (Adjust.gameStateTracker.state.game != HypixelGame.MURDER_MYSTERY) return true;
        var murderMystery = MurderMysteryModule.INSTANCE;
        boolean replaced = murderMystery.isEnabled() && murderMystery.autoStrike.autoStrikeEnabled.isEnabled();
        if (replaced) {
            var otherBind = murderMystery.autoStrike.optionalBind.getValue();
            if (otherBind.keys.isEmpty() || otherBind.sharesKeys(bind)) return false;
        }
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = name;
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.append(name).addText(" is replaced by ", 0xFFBEBCBD).append("Auto Strike")
                .addText(" in ", 0xFFBEBCBD).append("Murder Mystery").addText(" module.", 0xFFBEBCBD);
        notification.key = "killaura.murdermystery.autostrike";
        notification.show();
        return false;
    }
    @Override public boolean canToggle(boolean enabling) { return warnReplacedByAutoStrike(enabling); }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (!isEnabled()) return;
        if (settings.disableOnWorldJoin.isEnabled()) {
            disableConflictingModules(AutoBlockModule.class, TargetStrafeModule.class, NoSlowdownModule.class,
                    SpeedModule.class, VelocityModule.class);
            return;
        }
        targetSelector = new TargetSelector(this);
        aimInterpolator.sample = null;
        aimInterpolator.previousOffset = null;
        aimInterpolator.offset = null;
    }

    @Override public void onEnable() {
        targetSelector = new TargetSelector(this);
        swingWindow.timer.reset(); swingWindow.durationMs = -1L;
        swingDelay.timer.reset(); swingDelay.durationMs = -1L;
        yawSamples.clear();
        aimInterpolator.sample = null;
        aimInterpolator.previousOffset = null;
        aimInterpolator.offset = null;
        if (mc.thePlayer != null) yawSamples.add(mc.thePlayer.rotationYaw);
    }
    @Override public void onDisable() {
        targetSelector = null;
        aimInterpolator.sample = null;
        aimInterpolator.previousOffset = null;
        aimInterpolator.offset = null;
    }
    @Override public String getSuffix() { return settings.mode.getValue().getName(); }
}
