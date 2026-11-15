package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.*;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import static com.adjustclient.util.MinecraftInstance.mc;
import com.adjustclient.module.impl.movement.flight.FlightModule;
import net.minecraft.init.Blocks;
import com.adjustclient.module.impl.movement.longjump.LongJumpModule;

public final class BlinkAntiVoidMode extends AntiVoidMode {
    public static BlinkAntiVoidMode INSTANCE;
    FallSimulationResult safeState;
    public BlinkAntiVoidState blinkState;
    Timer flagTimer;
    Vec3 pendingMotion;
    final Timer lastFlagTimer = new Timer(0L);
    final BooleanSetting flagAsLastOption;
    int freezeTicks;
    int flagCooldownTicks;

    BlinkAntiVoidMode(AntiVoidModule module) {
        super(module, "Blink");
        INSTANCE = this;
        flagAsLastOption = new BooleanSetting(module, "Flag as last option", true, "On", "Off",
                () -> {
                    return module.mode.getValue() != this;
                });
        flagAsLastOption.setDescription("In the case that changing your motion will not work, attempt to flag the anti-cheat for a setback.");
    }

    @EventHandler(priority = 1) public void onMovementInput(MovementInputEvent event) {
        if (!isActive()) return;
        if (freezeTicks >= 1 && !InventoryStateTracker.inServerGui && !InventoryStateTracker.inServerInventory) {
            event.forward = false;
            event.back = false;
            event.left = false;
            event.right = false;
            event.jump = false;
            event.sneak = true;
        }
    }

    @EventHandler public void onWorldReset(WorldResetEvent event) {
        if (!isActive()) return;
        safeState = null;
        releasePackets();
    }

    @EventHandler public void onTeleport(TeleportEvent event) {
        if (!isActive() || !event.pre) return;
        Vec3 position = new Vec3(event.x, event.y, event.z);
        if (!module.isOverVoid(position)) {
            safeState = null;
            releasePackets();
            safeState = new FallSimulationResult(position, new Vec3(0.0D, 0.0D, 0.0D));
        }
        if (flagTimer != null && !flagTimer.hasElapsed(1200L)) {
            freezeTicks = 4;
            flagCooldownTicks = 15;
            pendingMotion = findMotion(position, new Vec3(0.0D, 0.0D, 0.0D),
                    new double[] {0.34D, 0.49D, 0.68D, 0.79D, 0.85D, 1.0D});
            flagTimer = null;
        }
    }

    @EventHandler public void onMoveFlying(MoveFlyingEvent event) {
        if (!isActive() || event.isCancelled()) return;
        if (!event.pre) {
            if (freezeTicks >= 1) mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0.0D;
            if (pendingMotion != null) {
                mc.thePlayer.motionX = pendingMotion.xCoord;
                mc.thePlayer.motionY = pendingMotion.yCoord;
                mc.thePlayer.motionZ = pendingMotion.zCoord;
                pendingMotion = null;
            }
            return;
        }
        --freezeTicks;
        if (flagCooldownTicks >= 1) --flagCooldownTicks;
        if (module.shouldIgnore()) {
            safeState = null;
            releasePackets();
            return;
        }
        if (!isFallingIntoVoid()) {
            safeState = new FallSimulationResult(mc.thePlayer.getPositionVector(),
                    new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ));
            if (blinkState != null) releasePackets();
            return;
        }
        if (blinkState == null && safeState != null) {
            Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
            blinkState = new BlinkAntiVoidState(simulation, safeState, new MovementStateSnapshot(mc.thePlayer));
            safeState = null;
            return;
        }
        if (blinkState != null && mc.thePlayer.fallDistance > module.fallDistance.getDouble() + 0.0005D) {
            if (canRecover()) {
                finishBlink();
                if (!module.isOverVoid(mc.thePlayer.getPositionVector())) return;
            }
        }
        if (blinkState != null && blinkState.timer.hasElapsed(1337L)) releasePackets();
        safeState = null;
    }

    private void recoverSimulation(Simulation simulation) {
        Vec3 motion = findMotion(simulation.player.getPositionVector(),
                new Vec3(simulation.player.motionX, simulation.player.motionY, simulation.player.motionZ), new double[0]);
        if (motion != null) {
            simulation.player.motionX = motion.xCoord;
            simulation.player.motionY = motion.yCoord;
            simulation.player.motionZ = motion.zCoord;
            return;
        }
        if (flagCooldownTicks < 1) {
            Vec3 target = findFlagPosition(simulation.player.getPositionVector());
            if (target != null && flagAsLastOption.isEnabled()) {
                lastFlagTimer.reset();
                simulation.player.lastTickPosX = target.xCoord;
                simulation.player.lastTickPosY = target.yCoord;
                simulation.player.lastTickPosZ = target.zCoord;
                simulation.player.setPosition(target.xCoord, target.yCoord, target.zCoord);
                flagTimer = new Timer();
            }
        }
    }

    @SuppressWarnings("unused")
    private static void vendorReportRecoveryTelemetry(double posX, double posY, double posZ, Vec3 target) {
        if (Adjust.telemetryListener != null) {
            Adjust.telemetryListener.report(new com.adjustclient.service.telemetry.event.AntiVoidRecord(
                    com.adjustclient.service.telemetry.TelemetryFlag.MODULE,
                    com.adjustclient.service.telemetry.TelemetryCategory.LOW,
                    (float) (posY - target.yCoord),
                    (float) Math.hypot(target.xCoord - posX, target.zCoord - posZ),
                    (float) target.yCoord));
        }
    }

    private boolean predictsVoid(EntityPlayer player) {
        Simulation simulation = new Simulation();
        simulation.simulateTick(player, true);
        Vec3 position = player.getPositionVector();
        boolean scaffold = ScaffoldModule.INSTANCE.isEnabled();
        boolean voidBelow = module.isOverVoid(position);
        if (!voidBelow && !scaffold) voidBelow = module.isOverVoid(position.addVector(player.motionX, player.motionY, player.motionZ));
        if (!voidBelow && !scaffold) {
            simulation.player.tick();
            voidBelow = module.isOverVoid(simulation.player.getPositionVector());
        }
        if (!voidBelow) return false;
        for (int i = 0; i < module.fallSimulations.getInt(); ++i) {
            simulation.player.tick();
            if (!module.isOverVoid(simulation.player.getPositionVector())) return false;
        }
        return true;
    }

    private Vec3 findFlagPosition(Vec3 position) {
        List<Vec3> offsets = new ArrayList<>();
        for (double radius : new double[] {0.8D, 1.2D, 1.6D}) {
            for (float angle = 0.0F; angle < 360.0F; angle += 45.0F) {
                double distance = RandomUtil.nextDouble(0.0D, 0.01D) + radius;
                double radians = angle * 0.0174532925199433D;
                offsets.add(new Vec3(-Math.sin(radians) * distance, 0.0D, Math.cos(radians) * distance));
            }
        }
        Vec3[] horizontal = offsets.toArray(Vec3[]::new);
        for (Vec3 offset : horizontal) offsets.add(offset.addVector(0.0D, -0.5D, 0.0D));
        for (Vec3 offset : offsets) {
            Vec3 candidate = position.add(offset);
            AxisAlignedBB box = new AxisAlignedBB(candidate.xCoord - 0.300000011920929D, candidate.yCoord,
                    candidate.zCoord - 0.300000011920929D, candidate.xCoord + 0.300000011920929D,
                    candidate.yCoord + 1.799999952316284D, candidate.zCoord + 0.300000011920929D)
                    .contract(0.0625D, 0.0625D, 0.0625D);
            if (BlockUtil.isColliding(box)) return candidate;
        }
        return null;
    }

    private boolean reachesGround(Simulation simulation, Vec3 position, Vec3 motion) {
        simulation.simulateTick(mc.thePlayer, true);
        SimulationPlayer player = simulation.player;
        player.lastTickPosX = position.xCoord;
        player.lastTickPosY = position.yCoord;
        player.lastTickPosZ = position.zCoord;
        player.setPosition(position.xCoord, position.yCoord, position.zCoord);
        player.motionX = motion.xCoord;
        player.motionY = motion.yCoord;
        player.motionZ = motion.zCoord;
        player.tick();
        return !module.isOverVoid(player.getPositionVector());
    }

    private Vec3 findMotion(Vec3 position, Vec3 motion, double[] extraSpeeds) {
        List<Double> speeds = new ArrayList<>(Arrays.asList(0.0D, 0.06D, 0.12D,
                MovementUtil.getSpeedWithPotion(0.21D, 0.2D)));
        for (double speed : extraSpeeds) speeds.add(MovementUtil.getSpeedWithPotion(speed, 0.2D));
        for (int i = 0; i < speeds.size(); ++i) speeds.set(i, speeds.get(i) + Math.random() * 0.0095D);
        List<Vec3> candidates = new ArrayList<>();
        for (double speed : speeds) {
            double angle = Math.atan2(motion.xCoord, -motion.zCoord) * 57.29577951308232D * 0.0174532925199433D;
            candidates.add(new Vec3(-motion.xCoord - Math.sin(angle) * speed, motion.yCoord,
                    -motion.zCoord + Math.cos(angle) * speed));
            angle = Math.atan2(-motion.xCoord, motion.zCoord) * 57.29577951308232D * 0.0174532925199433D;
            candidates.add(motion.addVector(-Math.sin(angle) * speed, 0.0D, Math.cos(angle) * speed));
        }
        for (double speed : speeds) {
            if (speed == 0.0D) continue;
            for (double angle = 0.0D; angle < 360.0D; angle += 45.0D) {
                double radians = angle * 0.0174532925199433D;
                candidates.add(new Vec3(-Math.sin(radians) * speed, motion.yCoord, Math.cos(radians) * speed));
            }
        }
        Simulation simulation = new Simulation();
        for (Vec3 candidate : candidates) if (reachesGround(simulation, position, candidate)) return candidate;
        return null;
    }

    private boolean isSafeEnvironment(EntityPlayer player) {
        if (player.onGround && player.isSneaking()) return true;
        net.minecraft.block.Block block = player.worldObj.getBlockState(new BlockPos(player.posX,
                player.getEntityBoundingBox().minY, player.posZ)).getBlock();
        if ((block == Blocks.ladder || block == Blocks.vine)
                && (player instanceof SimulationPlayer || !((net.minecraft.client.entity.AbstractClientPlayer) player).isSpectator())) return true;
        return player.inWater || player.ridingEntity != null || player.noClip;
    }

    private boolean isFallingIntoVoid() {
        if (!Features.ANTI_VOID_BLINK.isAllowed()) return false;
        if (mc.thePlayer.onGround) {
            if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) == 0.0D || mc.thePlayer.isSneaking()) return false;
        }
        if (isSafeEnvironment(mc.thePlayer) || FlightModule.INSTANCE.isEnabled() || LongJumpModule.INSTANCE.isEnabled()) return false;
        return predictsVoid(mc.thePlayer);
    }

    private boolean canRecover() {
        com.adjustclient.setting.NumberSetting distance = !module.voidDistOverrideKey.getValue().keys.isEmpty()
                && module.voidDistOverrideKey.isDown() ? module.voidDistOverride : module.voidDistance;
        double range = distance.getDouble();
        if (range < 12.0D) return true;
        double fraction = range / distance.getMax().doubleValue();
        Simulation simulation = new Simulation(mc.thePlayer).includeSpeedModule();
        boolean passedSafeGround = false;
        for (int tick = 0; tick < (passedSafeGround ? 45 : 25) * fraction; ++tick) {
            simulation.player.tick();
            if (simulation.player.onGround || isSafeEnvironment(simulation.player)) return false;
            if (tick >= 6 && !passedSafeGround && !predictsVoid(simulation.player)) passedSafeGround = true;
        }
        return true;
    }

    private void finishBlink() {
        BlinkAntiVoidState blink = blinkState;
        Simulation simulation = blink.simulation;
        SimulationPlayer player = simulation.player;
        player.setSneaking(false);
        player.movementInput.moveForward = 0.0F;
        player.movementInput.moveStrafe = 0.0F;
        player.isJumping = false;
        recoverSimulation(simulation);
        MovementStateSnapshot walking = blink.walkingState;
        mc.thePlayer.lastReportedPosX = walking.lastReportedX;
        mc.thePlayer.lastReportedPosY = walking.lastReportedY;
        mc.thePlayer.lastReportedPosZ = walking.lastReportedZ;
        mc.thePlayer.lastReportedYaw = walking.lastReportedYaw;
        mc.thePlayer.lastReportedPitch = walking.lastReportedPitch;
        mc.thePlayer.positionUpdateTicks = walking.positionUpdateTicks;
        mc.thePlayer.serverSprintState = walking.serverSprintState;
        List<com.adjustclient.game.packet.QueuedPacket> packets = new ArrayList<>();
        synchronized (Adjust.outboundPacketQueue.lock) {
            Adjust.outboundPacketQueue.sortHolds();
            if (!Adjust.outboundPacketQueue.holds.contains(blink.hold))
                throw new IllegalArgumentException("Rule #" + blink.hold.sequence + " is not valid!");
            for (com.adjustclient.game.packet.QueuedPacket packet : Adjust.outboundPacketQueue.packets)
                if (packet.sequence >= blink.hold.sequence) packets.add(packet);
        }
        BlinkPacketTransform transform = new BlinkPacketTransform(simulation, player, this);
        for (com.adjustclient.game.packet.QueuedPacket packet : packets) {
            ((java.util.concurrent.atomic.AtomicReference<com.adjustclient.game.packet.PacketTransform>) packet.sendArgument).set(transform);
        }
        Adjust.outboundPacketQueue.removeHold(blink.hold);
        module.isOverVoid(simulation.player.getPositionVector());
        blinkState = null;
        mc.thePlayer.setPosition(simulation.player.posX, simulation.player.posY, simulation.player.posZ);
        mc.thePlayer.motionY = player.motionY;
        MovementUtil.stop();
        safeState = new FallSimulationResult(mc.thePlayer.getPositionVector(),
                new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ));
    }

    void releasePackets() {
        if (blinkState != null) {
            Adjust.outboundPacketQueue.removeHold(blinkState.hold);
            blinkState = null;
        }
    }
}
