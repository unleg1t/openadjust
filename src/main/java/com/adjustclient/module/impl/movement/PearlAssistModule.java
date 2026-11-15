package com.adjustclient.module.impl.movement;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.bind.BindMode;
import com.adjustclient.event.*;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.*;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import com.adjustclient.util.simulation.ProjectileSimulation;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraft.util.ChatColor;

public final class PearlAssistModule extends Module {
    public static PearlAssistModule INSTANCE;
    public Vec3 targetPosition;
    public com.adjustclient.util.anticheat.entity.TrackedEntity<?> target;
    public PearlTrajectory trajectory;
    private final ProjectileSimulation simulation;
    public final KeybindSetting throwBind;
    public final ExpandSetting simulationSettings;
    public final ModeSetting<PearlSimulationIntensity> intensity;
    public final NumberSetting expand;
    public ExpandSetting targets;
    public BooleanSetting players;
    public BooleanSetting passive;
    public BooleanSetting hostile;
    public BooleanSetting invisibles;
    public BooleanSetting teammates;
    public ExpandSetting safetyTimer;
    public NumberSetting slowdown;
    public NumberSetting safewalk;
    public NumberSetting jumpRestrict;
    public NumberSetting maxTime;
    public NumberSetting cooldown;
    public BooleanSetting silentAim;
    public final List<PearlThrowPlan> plans = new ArrayList<>();
    public com.adjustclient.game.packet.PacketHold hold;
    public final Timer throwTimer = new Timer();
    public final Timer targetTimer = new Timer();
    public int targetDistance;
    public int queuedMovements;
    public boolean bindHeld;
    public boolean throwPending;
    public boolean searchRequested;
    public boolean safetyActive;
    public boolean simulating;

    public PearlAssistModule() {
        super("Pearl Assist", Category.MOVEMENT);
        INSTANCE = this;
        simulation = new ProjectileSimulation();
        Bind bind = Adjust.INSTANCE.bindManager.register("module.pearlassist.keybind", mode -> {
            if (isEnabled()) onThrowBind(mode);
        }, "Pearl Assist Throw Bind");
        throwBind = new KeybindSetting(this, "Throw Bind", bind);
        simulationSettings = new ExpandSetting(this, "Simulation");
        intensity = new ModeSetting<>(simulationSettings, "Intensity", PearlSimulationIntensity.EASY,
                PearlSimulationIntensity.values());
        expand = new NumberSetting(simulationSettings, "Expand", 3.5, 0.0, 5.0, 0.5);
        targets = new ExpandSetting(this, "Targets");
        players = new BooleanSetting(targets, "Players", true);
        passive = new BooleanSetting(targets, "Passive", false);
        hostile = new BooleanSetting(targets, "Hostile", false);
        invisibles = new BooleanSetting(targets, "Invisibles", true);
        teammates = new BooleanSetting(targets, "Teammates", true);
        safetyTimer = new ExpandSetting(this, "Safety timer");
        slowdown = new NumberSetting(safetyTimer, "Slowdown", 0.3, 0.0, 1.0, 0.05);
        slowdown.formatter(NumberSetting.SECONDS);
        safewalk = new NumberSetting(safetyTimer, "Safewalk", 1.0, 0.0, 3.0, 0.1);
        safewalk.formatter(NumberSetting.SECONDS);
        jumpRestrict = new NumberSetting(safetyTimer, "Jump restrict", 1.0, 0.0, 3.0, 0.1);
        jumpRestrict.formatter(NumberSetting.SECONDS);
        maxTime = new NumberSetting(this, "Max. time", 5.0, 0.5, 6.5, 0.1);
        maxTime.formatter(NumberSetting.SECONDS);
        cooldown = new NumberSetting(this, "Cooldown", 1.0, 0.0, 5.0, 0.1);
        cooldown.formatter(NumberSetting.SECONDS);
        silentAim = new BooleanSetting(this, "Silent Aim", false);
        safewalk.field36 = (java.util.function.Supplier<String>) () -> "Safe Walk";
        silentAim.addListener(() -> {
            if (!silentAim.isEnabled() && hold != null) {
                Adjust.outboundPacketQueue.removeHold(hold);
                queuedMovements = 0;
            }
        });
    }

    public void notifyNoPearl() {
        com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
        notification.title = getName();
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.key = "pearlassist.noenderpearl";
        notification.lifetimeSteps = 78;
        notification.addText("You must have at least one Ender Pearl in your hotbar.", 0xFFBEBCBD).show();
    }

    @EventHandler(priority = 45) public void onTick(IngameActionEvent event) {
        if (!isEnabled()) return;
        if (hold != null && ++queuedMovements >= 4) releaseHold();
        if (bindHeld && simulating && !searchRequested) {
            simulating = false;
            bindHeld = false;
            target = null;
            targetPosition = null;
            trajectory = null;
            return;
        }
        if (!simulating || target != null && target.stale) {
            target = null;
            targetPosition = null;
            trajectory = null;
        }
        if (!simulating) return;
        Vec3 origin = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        int filter = com.adjustclient.util.anticheat.entity.EntityFilter.of(false, players.isEnabled(),
                hostile.isEnabled(), passive.isEnabled(), false, teammates.isEnabled(), invisibles.isEnabled());
        com.adjustclient.util.anticheat.entity.TrackedEntity<?> bestTarget = null;
        float bestAngle = 999.0F;
        for (var tracked : com.adjustclient.util.anticheat.ServerTracker.INSTANCE.entityTracker.getEntities(filter, null)) {
            Float angle = tracked.previewData;
            if (angle == null) {
                if (mc.thePlayer == null) angle = 999.0F;
                else {
                    Entity entity = tracked.getEntity();
                    Vec2f rotation = RotationUtil.getRotations(new Vec3(entity.posX,
                            entity.posY + entity.getEyeHeight(), entity.posZ));
                    angle = RotationUtil.getRotationDistance(rotation, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                }
                tracked.previewData = angle;
            }
            if (angle >= 30.0F || bestAngle <= angle) continue;
            Entity entity = tracked.getEntity();
            double dx = origin.xCoord - entity.posX, dz = origin.zCoord - entity.posZ;
            float distance = (float) Math.sqrt(dx * dx + dz * dz);
            if (distance <= 6.5F || distance > expand.getValue().doubleValue() + 55.0) continue;
            if (!com.adjustclient.render.RenderUtil.isInFrustum(entity)) continue;
            bestTarget = tracked;
            bestAngle = angle;
        }
        if (bestTarget != null) {
            Entity entity = bestTarget.getEntity();
            targetPosition = new Vec3(entity.posX, entity.posY, entity.posZ);
            target = bestTarget;
        } else {
            targetPosition = null;
            searchRequested = false;
        }
        Iterator<PearlThrowPlan> pending = plans.iterator();
        while (pending.hasNext()) {
            PearlThrowPlan plan = pending.next();
            if (plan.timer.hasElapsed(plan.flightMillis + 685)) {
                plans.remove(plan);
                break;
            }
        }
        if (targetPosition != null) targetDistance = (int) origin.distanceTo(targetPosition);
        if (targetPosition == null || trajectory == null || !searchRequested) return;
        throwTimer.reset();
        searchRequested = false;
        int budget = intensity.getValue().simulationSteps;
        List<PearlSimulationState> candidates = new ArrayList<>();
        List<BlockPos> positions = trajectory.positions;
        positions.sort(Comparator.comparingDouble(pos -> {
            double dx = pos.getX() - targetPosition.xCoord;
            double dy = pos.getY() - targetPosition.yCoord;
            double dz = pos.getZ() - targetPosition.zCoord;
            return dx * dx + dy * dy + dz * dz;
        }));
        long start = System.currentTimeMillis();
        boolean stop = false;
        for (int i = 0; i < positions.size(); i++) {
            BlockPos pos = positions.get(i);
            List<PearlSimulationState> results = new ArrayList<>();
            simulateThrows(new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), origin, results);
            for (PearlSimulationState state : results) {
                Vec3 hit = state.impact;
                if (hit.yCoord >= targetPosition.yCoord - 1.0 && hit.distanceTo(targetPosition) < 3.5
                        || state.flightTicks < 15) {
                    stop = true;
                    break;
                }
            }
            candidates.addAll(results);
            if (stop) break;
            if (i % 5 == 0 && System.currentTimeMillis() - start > budget) {
                simulating = false;
                return;
            }
        }
        PearlSimulationState best = null;
        double bestTime = 999.0;
        for (PearlSimulationState state : candidates) {
            if (bestTime > state.flightTicks) {
                bestTime = state.flightTicks;
                best = state;
            }
        }
        if (best == null) {
            com.adjustclient.ui.hud.notification.NotificationBuilder notification = new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = getName();
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Failed to find a viable pearl throw!", 0xFFBEBCBD)
                    .addText(" (" + candidates.size() + " tests)", 0xFF8A898A).show();
            simulating = false;
            return;
        }
        if (silentAim.isEnabled()) acquireHold();
        com.adjustclient.rotation.handler.RotationHandler.rotate(com.adjustclient.rotation.InstantRotationModel.INSTANCE,
                best.yaw, best.pitch);
        throwPending = true;
        plans.add(new PearlThrowPlan(best.launch, best.impact,
                best.flightTicks, target == null ? 0 : target.entityId,
                origin.distanceTo(best.impact)));
    }

    @EventHandler public void onTeleport(TeleportEvent event) {
        if (!isEnabled() || plans.isEmpty() || !event.pre) return;
        PearlThrowPlan closest = null;
        double best = 999.0;
        for (PearlThrowPlan plan : plans) {
            if (plan.flightMillis < 500) continue;
            Vec3 hit = plan.impact;
            double dx = event.x - hit.xCoord;
            double dy = event.y - hit.yCoord;
            double dz = event.z - hit.zCoord;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 3.5 && best > distance) {
                best = distance;
                closest = plan;
            }
        }
        if (closest != null) {
            activateSafety();
            plans.remove(closest);
        }
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        if (slowdown.getValue().doubleValue() > 0.0) {
            for (PearlThrowPlan plan : plans) {
                if (plan.flightMillis < 1) continue;
                long remaining = plan.flightMillis
                        - (System.currentTimeMillis() - plan.timer.lastMs);
                if (remaining < 51 || remaining > slowdown.getValue().doubleValue() * 1000.0) continue;
                if (mc.thePlayer.onGround && !MovementFixModule.INSTANCE.isEnabled()) {
                    mc.thePlayer.motionX *= 0.3;
                    mc.thePlayer.motionZ *= 0.3;
                }
                break;
            }
        }
        if (!throwPending) return;
        throwPending = false;
        simulating = false;
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        if (stack == null || !(stack.getItem() instanceof ItemEnderPearl)) {
            Integer slot = findPearlSlot();
            if (slot == null) {
                notifyNoPearl();
                targetPosition = null;
                simulating = false;
                return;
            }
            Adjust.slotSpoofer.slot = slot;
            Adjust.slotSpoofer.active = true;
            Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.renderSpoofed = false;
        }
        Adjust.mouseClickHelper.right.clicks = 1;
        Adjust.mouseClickHelper.right.down = true;
        Adjust.mouseClickHelper.right.holdTicks = 0;
    }

    @EventHandler public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || (targetPosition == null && plans.isEmpty())) return;
        java.awt.Color accent = com.adjustclient.module.impl.render.hud.HUDModule.animatedColor;
        if (accent == null) accent = com.adjustclient.module.impl.render.hud.HUDModule.colorSetting.getColor();
        int count = plans.size();
        org.joml.Vector4d[] projected = new org.joml.Vector4d[count + 1];
        mc.entityRenderer.setupCameraTransform(event.partialTicks, 0);
        boolean drawTargetLabel = true;
        boolean any = false;
        for (int i = 0; i < count; i++) {
            PearlThrowPlan plan = plans.get(i);
            if (target != null && target.entityId == plan.targetId) drawTargetLabel = false;
            projected[i] = com.adjustclient.render.RenderUtil.worldToScreen(plan.impact,
                    new Vec2f(0.0F, 0.0F), event.scaledResolution);
            any = true;
        }
        if (targetPosition != null) {
            projected[count] = com.adjustclient.render.RenderUtil.worldToScreen(targetPosition,
                    new Vec2f(0.0F, 0.0F), event.scaledResolution);
            any = true;
        }
        mc.entityRenderer.setupOverlayRendering(event.scaledResolution);
        if (!any) return;
        if (targetPosition != null && projected[count] != null) {
            java.awt.Color lineColor = accent;
            long delay = (long) (cooldown.getValue().doubleValue() * 1000.0);
            if (cooldown.getValue().doubleValue() > 0.001 && !throwTimer.hasElapsed(delay)) {
                float factor = (float) ((double) (System.currentTimeMillis() - throwTimer.lastMs) / delay);
                lineColor = com.adjustclient.render.ColorUtil.blend(accent,
                        com.adjustclient.render.ColorUtil.withAlpha(accent, 0), factor);
            }
            com.adjustclient.render.RenderUtil.glColor(lineColor.getRGB());
            com.adjustclient.render.RenderUtil.drawLine2d((float) (event.scaledResolution.getScaledWidth_double() / 2.0),
                    (float) (event.scaledResolution.getScaledHeight_double() / 2.0), (float) projected[count].x,
                    (float) (projected[count].y - 0.5), 1.0F);
            net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        for (int i = 0; i < projected.length; i++) {
            org.joml.Vector4d screen = projected[i];
            if (screen == null || i == count && !drawTargetLabel) continue;
            PearlThrowPlan plan = i < count ? plans.get(i) : null;
            String label = getLabel(plan);
            float progress = 1.0F;
            if (plan != null) {
                long remaining = plan.flightMillis
                        - (System.currentTimeMillis() - plan.timer.lastMs);
                progress = remaining >= 1 ? (float) remaining / plan.flightMillis : 0.0F;
            }
            float halfWidth = mc.fontRendererObj.getStringWidth(label, 0.5F) * 0.5F;
            float centerX = (float) screen.x + halfWidth * 0.5F;
            float left = centerX - halfWidth - 1.5F;
            float right = centerX + halfWidth + 1.5F;
            float top = (float) screen.y;
            com.adjustclient.render.font.image.NvgDraw.fillRect(0x91000000, left, top, right, top + 7.5F);
            com.adjustclient.render.font.image.NvgDraw.fillRect(left, top, right, top + 0.5F);
            com.adjustclient.render.font.image.NvgDraw.fillRect(accent.getRGB(), left, top,
                    left + (right - left) * progress, top + 0.5F);
            final float x = left, y = top;
            event.postTasks.add(() -> {
                net.minecraft.client.renderer.GlStateManager.enableBlend();
                mc.fontRendererObj.drawString(label, x + 1.5F, y + 2.0F, 0xFFE9E9EA, 0.5F);
            });
        }
    }

    public String getLabel(PearlThrowPlan plan) {
        if (plan == null) return String.valueOf(ChatColor.GRAY) + targetDistance + "m";
        long remaining = plan.flightMillis
                - (System.currentTimeMillis() - plan.timer.lastMs);
        String time = remaining >= 1 ? com.adjustclient.util.TimeUtil.formatDuration(remaining, true) : "0.0s";
        return time + ChatColor.GRAY + " " + Math.round(plan.distance) + "m";
    }

    public void simulateThrows(Vec3 destination, Vec3 origin, List<PearlSimulationState> results) {
        int limit = (int) (maxTime.getValue().doubleValue() * 1000.0 / 50.0);
        Vec2f rotation = RotationUtil.getYawBetween(destination, origin);
        double dx = destination.xCoord - origin.xCoord, dz = destination.zCoord - origin.zCoord;
        float initialPitch = MathHelper.clamp_float(rotation.y - (float) (Math.sqrt(dx * dx + dz * dz) / 6.0), -90.0F, 90.0F);
        float step = (90.0F + initialPitch) / 15.0F;
        for (float pitch = initialPitch; pitch > -90.0F; pitch -= step) {
            float eyeHeight = mc.thePlayer == null ? 1.53F : mc.thePlayer.getEyeHeight();
            simulation.setThrowableHeading(mc.theWorld, eyeHeight, origin.xCoord, origin.yCoord, origin.zCoord, rotation.x, pitch);
            simulation.source = mc.thePlayer;
            Vec3 launch = new Vec3(simulation.posX, simulation.posY, simulation.posZ);
            boolean collided = false;
            int tick = 0;
            for (; tick < limit; tick++) {
                if (simulation.onUpdate()) {
                    collided = true;
                    break;
                }
            }
            MovingObjectPosition hit = simulation.collision;
            if (!collided || hit == null || hit.getBlockPos() == null || hit.sideHit == null) continue;
            BlockPos adjacent = hit.getBlockPos().offset(hit.sideHit);
            Vec3 impact = new Vec3(adjacent.getX(), adjacent.getY(), adjacent.getZ());
            if (destination.yCoord > impact.yCoord || targetPosition.distanceTo(impact) > 3.75) continue;
            Vec3 below = impact.addVector(0.0, -1.0, 0.0);
            if (!mc.theWorld.getBlockState(new BlockPos(below)).getBlock().getMaterial().isSolid()) continue;
            if (targetPosition.distanceTo(below) > 6.5) continue;
            Vec3 rounded = new Vec3((int) Math.round(simulation.posX), simulation.posY, (int) Math.round(simulation.posZ));
            if (destination.yCoord > rounded.yCoord) continue;
            float distance = (float) targetPosition.distanceTo(rounded);
            if (distance > 3.75F) continue;
            Vec3 solid = rounded.addVector(0.0, 1.0, 0.0);
            if (!mc.theWorld.getBlockState(new BlockPos(solid)).getBlock().getMaterial().isSolid()) {
                solid = rounded;
                if (!mc.theWorld.getBlockState(new BlockPos(solid)).getBlock().getMaterial().isSolid()) {
                    solid = rounded.addVector(0.0, -1.0, 0.0);
                    if (!mc.theWorld.getBlockState(new BlockPos(solid)).getBlock().getMaterial().isSolid()) continue;
                }
            }
            if (targetPosition.distanceTo(solid) > 6.5) continue;
            results.add(new PearlSimulationState(tick, launch, rounded, distance, rotation.x, pitch));
        }
    }

    public void onThrowBind(BindMode mode) {
        if (mode == BindMode.PRESSED) {
            if (mc.thePlayer == null || mc.thePlayer.capabilities.isCreativeMode) return;
            Integer slot = WorldUtil.findHotbarSlot(mc.thePlayer.inventory.m(), index -> {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[index];
                return stack != null && stack.getItem() instanceof ItemEnderPearl;
            });
            if (slot == null) notifyNoPearl();
            else {
                bindHeld = false;
                simulating = true;
            }
            return;
        }
        if (!simulating || searchRequested) return;
        if (targetPosition == null) {
            simulating = false;
            return;
        }
        BlockPos center = new BlockPos(targetPosition);
        int radius = (int) (expand.getValue().doubleValue() * 2.0 + 3.0);
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    BlockPos base = center.add(x, y, z);
                    if (!mc.theWorld.getBlockState(base).getBlock().getMaterial().isSolid()) continue;
                    BlockPos above = base.up();
                    if (mc.theWorld.getBlockState(above).getBlock().getMaterial().isSolid()) continue;
                    for (int height = 0; height < 5; height++) {
                        for (EnumFacing side : EnumFacing.values()) {
                            if (side == EnumFacing.DOWN || side == EnumFacing.UP) continue;
                            BlockPos neighbor = above.offset(side);
                            if (mc.theWorld.getBlockState(neighbor).getBlock().getMaterial().isSolid()) positions.add(neighbor);
                        }
                        above = above.up();
                    }
                    positions.add(base);
                }
            }
        }
        trajectory = new PearlTrajectory(positions);
        long delay = (long) (cooldown.getValue().doubleValue() * 1000.0);
        if (cooldown.getValue().doubleValue() <= 0.001 || throwTimer.hasElapsed(delay)) {
            searchRequested = true;
            bindHeld = true;
        } else simulating = false;
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || mc.thePlayer == null || plans.isEmpty() || event.outgoing) return;
        if (!(event.packet instanceof net.minecraft.network.play.server.S0EPacketSpawnObject spawn)
                || spawn.getType() != 65) return;
        Vec3 position = new Vec3(spawn.getX() / 32.0, spawn.getY() / 32.0, spawn.getZ() / 32.0);
        PearlThrowPlan closest = null;
        double bestDistance = 999.0;
        for (PearlThrowPlan plan : plans) {
            double distance = position.distanceTo(plan.launch);
            if (distance <= 2.5 && bestDistance > distance) {
                bestDistance = distance;
                closest = plan;
            }
        }
        if (closest != null) {
            closest.flightMillis = 50 * closest.flightTicks;
            closest.timer.reset();
        }
    }

    @Override public void onEnable() {
        targetPosition = null;
        throwPending = false;
        safetyActive = false;
        bindHeld = false;
        searchRequested = false;
        simulating = false;
    }

    @Override public void onDisable() {
        plans.clear();
        releaseHold();
    }

    public void releaseHold() {
        if (hold != null) {
            Adjust.outboundPacketQueue.removeHold(hold);
            queuedMovements = 0;
        }
    }

    public void acquireHold() {
        if (hold != null) return;
        queuedMovements = 0;
        hold = Adjust.outboundPacketQueue.addHold(null, false);
    }

    public void activateSafety() {
        safetyActive = true;
        targetTimer.reset();
        long duration = (long) (safewalk.getValue().doubleValue() * 1000.0);
        if (duration >= 6) SafeWalkModule.INSTANCE.cooldown = new com.adjustclient.util.Cooldown(duration);
    }

    public static Integer findPearlSlot() {
        if (mc.thePlayer == null) return null;
        int start = mc.thePlayer.inventory.m();
        for (int i = start; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemEnderPearl) return i;
        }
        if (start < 0) return null;
        for (int i = start; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemEnderPearl) return i;
        }
        return null;
    }

    @EventHandler public void onWorldChange(WorldResetEvent event) {
        if (!isEnabled()) return;
        releaseHold();
        plans.clear();
    }
}
