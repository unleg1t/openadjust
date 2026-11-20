package com.adjustclient.modules.impl.combat.targetstrafe;

import com.adjustclient.Adjust;
import com.adjustclient.core.bind.Bind;
import com.adjustclient.core.bind.KeyboardKey;
import com.adjustclient.core.events.*;
import com.adjustclient.utilities.game.entity.tracker.ServerPositionTracker;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.rendering.RenderUtil;
import com.adjustclient.core.settings.*;
import com.adjustclient.utilities.*;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.anticheat.entity.TrackedLivingEntity;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import com.adjustclient.modules.impl.combat.killaura.KillAuraModule;
import com.adjustclient.modules.impl.movement.flight.FlightModule;
import com.adjustclient.modules.impl.movement.speed.SpeedModule;
import com.adjustclient.modules.impl.movement.longjump.LongJumpModule;

public final class TargetStrafeModule extends Module implements MinecraftInstance {
    public static TargetStrafeModule INSTANCE;
    public StrafeState strafeState;
    public final NumberSetting radius;
    public final KeybindSetting holdBind;
    public final BooleanSetting alwaysWithFlight;
    public final BooleanSetting speedModuleOnly;
    public final BooleanSetting behind;
    public final ExpandSetting targets;
    public final BooleanSetting targetsPlayers;
    public final BooleanSetting targetsTeammates;
    public final BooleanSetting targetsInvisibles;
    public final BooleanSetting targetsKillAura;
    public final ExpandSetting render;
    public final BooleanSetting renderEnabled;
    public final ColorSetting renderColor;

    public TargetStrafeModule() {
        super("Target Strafe", Category.COMBAT);
        INSTANCE = this;
        notifyToggle = false;
        radius = new NumberSetting(this, "Radius", 1.0D, 0.5D, 4.0D, 0.1D).formatter(NumberSetting.METERS);
        alwaysWithFlight = new BooleanSetting(this, "Always with Flight", false, "On", "Off");
        speedModuleOnly = new BooleanSetting(this, "Speed module only", true, "On", "Off");
        behind = new BooleanSetting(this, "Behind", false, "On", "Off");
        targets = new ExpandSetting(this, "Targets");
        targetsPlayers = new BooleanSetting(targets, "Players", true, "On", "Off");
        targetsTeammates = new BooleanSetting(targets, "Teammates", false, "On", "Off");
        targetsInvisibles = new BooleanSetting(targets, "Invisibles", true, "On", "Off");
        targetsKillAura = new BooleanSetting(targets, "Kill Aura", false, "On", "Off");
        targetsKillAura.setDescription("Prioritize the entity being targeted by Kill Aura.");
        render = new ExpandSetting(this, "Render");
        renderEnabled = new BooleanSetting(render, "Enabled", true, "Yes", "No");
        render.enabledPredicate = () -> this.renderEnabled.isEnabled() && !this.renderEnabled.isHidden();
        renderColor = new ColorSetting(render, "Color", new Color(0xffaaaaaa, true));
        Bind bind = Adjust.INSTANCE.bindManager.register(
                "module.targetstrafe.holdbind", mode -> { }, "Target Strafe Hold Bind");
        bind.addKey(new KeyboardKey(org.lwjgl.input.Keyboard.KEY_SPACE));
        holdBind = new KeybindSetting(this, "Hold Bind", bind);
    }
    public void updateTarget() {
        var aura = KillAuraModule.INSTANCE;
        var priorityInfo = aura.isEnabled() ? aura.targetSelector.rotationTarget : null;
        TrackedLivingEntity priority = priorityInfo == null ? null : priorityInfo.entity;
        if (targetsKillAura.isEnabled() && !targetsKillAura.isHidden() && priority != null && !priority.isRemoved()) {
            if (strafeState == null || strafeState.target != priority)
                strafeState = new StrafeState(priority);
            return;
        }
        int mask = 1024;
        if (targetsPlayers.isEnabled() && !targetsPlayers.isHidden()) mask |= 2;
        if (targetsTeammates.isEnabled() && !targetsTeammates.isHidden()) mask |= 32;
        if (targetsInvisibles.isEnabled() && !targetsInvisibles.isHidden()) mask |= 64;
        var entities = ServerTracker.INSTANCE.entityTracker.getEntities(mask, TrackedLivingEntity.class);
        entities.sort(Comparator.comparingDouble(entity -> entity.getEntity().getDistanceToEntity(mc.thePlayer)));
        for (TrackedLivingEntity entity : entities) {
            if (Math.sqrt(mc.thePlayer.getDistanceSqToEntity(entity.getEntity())) > 4.5D) continue;
            if (strafeState == null || strafeState.target != entity)
                strafeState = new StrafeState(entity);
            return;
        }
        strafeState = null;
    }
    public boolean shouldGoBehind() {
        return behind.isEnabled() && !behind.isHidden()
                && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown();
    }
    public float updateYaw() {
        StrafeState state = strafeState;
        Vec3 point = getOrbitPoint(mc.thePlayer, state.target.getEntity(), state.direction);
        double dx = point.xCoord - mc.thePlayer.posX, dz = point.zCoord - mc.thePlayer.posZ;
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        state.movementYaw = yaw;
        return yaw;
    }
    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        if (!canStrafe()) { strafeState = null; return; }
        updateTarget();
        if (strafeState == null) return;
        if (shouldGoBehind() || !updateDirection()) updateYaw();
    }
    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isEnabled() || !renderEnabled.isEnabled() || renderEnabled.isHidden() || strafeState == null) return;
        EntityLivingBase entity = strafeState.target.getEntity();
        RenderUtil.j();
        double x = entity.posX, y = entity.posY, z = entity.posZ;
        double oldX = entity.lastTickPosX, oldY = entity.lastTickPosY, oldZ = entity.lastTickPosZ;
        var entry = ServerPositionTracker.INSTANCE == null ? null : ServerPositionTracker.INSTANCE.findTrackedPosition(entity);
        if (entry != null) { x = entry.x; y = entry.y; z = entry.z; oldX = entry.previousX; oldY = entry.previousY; oldZ = entry.previousZ; }
        var manager = mc.getRenderManager();
        RenderUtil.drawLine3d(renderColor.getColor().getRGB(),
                (x - oldX) * event.partialTicks + oldX - manager.viewerPosX,
                (y - oldY) * event.partialTicks + oldY + 0.01D - manager.viewerPosY,
                (z - oldZ) * event.partialTicks + oldZ - manager.viewerPosZ, radius.getDouble(), 2.0F);
        RenderUtil.k();
    }
    public StrafePathCheck checkPath(StrafeDirection direction) {
        EntityLivingBase target = strafeState.target.getEntity();
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        int unsafe = 0;
        for (int i = 0; i < 17; i++) {
            simulateOrbitTick(simulation, target, direction);
            var player = simulation.player;
            boolean bad = player.isCollidedHorizontally || player.fallDistance > 1.5F;
            if (!bad) {
                double halfWidth = mc.thePlayer.width * 0.5F;
                AxisAlignedBB column = new AxisAlignedBB(player.posX - halfWidth, 0.0D, player.posZ - halfWidth,
                        player.posX + halfWidth, player.posY, player.posZ + halfWidth);
                bad = !BlockUtil.isColliding(column) || mc.thePlayer.inWater || mc.thePlayer.isInLava() || mc.thePlayer.isInWeb;
            }
            if (bad) { if (++unsafe >= 16) return new StrafePathCheck(true); }
            else unsafe = 0;
        }
        return new StrafePathCheck(false);
    }
    public void simulateOrbitTick(Simulation simulation, EntityLivingBase target, StrafeDirection direction) {
        var player = simulation.player;
        Vec3 point = getOrbitPoint(player, target, direction);
        player.movementInput.moveForward = 1.0F;
        player.movementInput.moveStrafe = 0.0F;
        double dx = point.xCoord - player.posX, dz = point.zCoord - player.posZ;
        player.rotationYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        player.ticksExisted++;
        player.onUpdate();
    }
    public Vec3 getOrbitPoint(EntityPlayer player, EntityLivingBase target, StrafeDirection direction) {
        var entry = Adjust.entityTrackerService.tracker.entries.get(target.getEntityId());
        double x = entry == null ? target.posX : entry.x;
        double y = entry == null ? target.posY : entry.y;
        double z = entry == null ? target.posZ : entry.z;
        double radius = this.radius.getDouble();
        float yaw;
        if (shouldGoBehind()) yaw = target.rotationYaw - 180.0F;
        else yaw = (float) (Math.atan2(z - player.posZ, x - player.posX) * 180.0D / Math.PI) - 90.0F + direction.angle;
        double radians = yaw * 0.017453292519943295D;
        return new Vec3(-Math.sin(radians) * radius + x, y, Math.cos(radians) * radius + z);
    }
    @Override public void onDisable() { strafeState = null; }
    public boolean updateDirection() {
        StrafeState state = strafeState;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) { state.direction = StrafeDirection.LEFT; return false; }
        if (mc.gameSettings.keyBindRight.isKeyDown()) { state.direction = StrafeDirection.RIGHT; return false; }
        StrafePathCheck check = checkPath(state.direction);
        if (!isEnabled()) return true;
        if (!check.blocked) return false;
        StrafeDirection opposite = state.direction == StrafeDirection.LEFT ? StrafeDirection.RIGHT : StrafeDirection.LEFT;
        if (checkPath(opposite).blocked) { strafeState = null; return true; }
        state.direction = opposite;
        return false;
    }
    public boolean canStrafe() {
        var player = mc.thePlayer;
        if (player.moveForward <= 0.0F || player.inWater) return false;
        if (player.worldObj.checkBlockCollision(player.getEntityBoundingBox().expand(-0.1000000014901161D, -0.4000000059604645D, -0.1000000014901161D))) return false;
        if (player.capabilities.isFlying) return false;
        var game = Adjust.gameStateTracker.state;
        if (game.isSpecialState() != 0 || game.isSpectator() || game.lobby != null
                && !game.lobby && game.startTimer == null) return false;
        var rewound = Adjust.rewoundGameTracker.state;
        if (rewound.isInSpawn() || rewound.allowFlying || rewound.lobby != null
                && !rewound.lobby && rewound.startTimer == null) return false;
        if (speedModuleOnly.isEnabled() && !hasMovementModule()) return false;
        if (alwaysWithFlight.isEnabled() && (LongJumpModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled())) return true;
        return holdBind.isDown() || holdBind.getValue().keys.isEmpty();
    }
    public static boolean hasMovementModule() {
        return SpeedModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled() || FlightModule.INSTANCE.isEnabled();
    }
}
