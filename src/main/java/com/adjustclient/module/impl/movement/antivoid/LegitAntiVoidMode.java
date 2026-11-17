package com.adjustclient.module.impl.movement.antivoid;

import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.game.render.FrozenPlayerRenderer;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.util.*;
import com.adjustclient.util.Timer;
import java.util.*;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import static com.adjustclient.util.MinecraftInstance.mc;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.module.impl.movement.SafeWalkModule;

public final class LegitAntiVoidMode extends AntiVoidMode {
    public static LegitAntiVoidMode INSTANCE;
    public ClutchPlacement safeSpot;
    public final Timer timer = new Timer(0L);
    final Timer failureTimer = new Timer(0L);
    final FrozenPlayerRenderer renderer;
    final BooleanSetting displayFreeze;
    final BooleanSetting blockClutch;
    final NumberSetting maxFallDist;

    private static final java.util.function.Predicate<IBlockState> WATER_BLOCK_PREDICATE = state -> {
        return state.getBlock().getMaterial() == Material.water;
    };

    LegitAntiVoidMode(AntiVoidModule module) {
        super(module, "Legit");
        INSTANCE = this;
        renderer = Adjust.frozenPlayerRenderListener.renderers.computeIfAbsent("AntiVoid", Adjust.frozenPlayerRenderListener.factory);
        collisionWidth = 0.45F;
        displayFreeze = new BooleanSetting(module, "Display Freeze", false, "On", "Off",
                () -> {
                    return module.mode.getValue() != this;
                });
        displayFreeze.setDescription("Renders where you will jump or move");
        blockClutch = new BooleanSetting(module, "Block Clutch", true, "On", "Off",
                () -> {
                    return module.mode.getValue() != this;
                });
        blockClutch.setDescription("Places blocks below you to save you");
        maxFallDist = new NumberSetting(module, "Max. Fall Dist.", 20, 1, 41, 1,
                () -> {
                    return module.mode.getValue() != this;
                });
        maxFallDist.setDescription("Prioritizes movement with fall distances below this value.");
        maxFallDist.formatter(setting -> {
            return setting.getInt() == setting.getMax().intValue() ? "Disabled" : Integer.toString(setting.getInt());
        });
    }

    @Override public void onEnable() {
        safeSpot = null;
    }

    @Override public void onDisable() {
        timer.lastMs = 0L;
    }

    @EventHandler public void onTeleport(TeleportEvent event) {
        if (!isActive()) return;
        failureTimer.lastMs = 0L;
        safeSpot = null;
    }

    @EventHandler public void onVelocity(IncomingVelocityEvent event) {
        if (!isActive() || event.isCancelled()) return;
        if (event.hasVelocity()) safeSpot = null;
    }

    @EventHandler(priority = 1) public void onPlayerUpdate(SprintEligibilityEvent event) {
        if (!isActive()) return;
        if (safeSpot != null && safeSpot.preview != null) {
            KeyBinding sprint = mc.gameSettings.keyBindSprint;
            if (!safeSpot.preview.sprint) sprint.pressTime = 0;
            KeyBinding.setKeyBindState(sprint.getKeyCode(), safeSpot.preview.sprint);
        }
    }

    @EventHandler public void onJumpDirection(JumpMotionEvent event) {
        if (!isActive()) return;
        if (event.pre && !MovementFixModule.INSTANCE.isEnabled() && safeSpot != null && safeSpot.preview != null)
            event.yaw = safeSpot.preview.rotation.x;
    }

    @EventHandler public void onMoveFlying(MoveFlyingEvent event) {
        if (!isActive() || event.isCancelled()) return;
        if (event.pre && !MovementFixModule.INSTANCE.isEnabled() && safeSpot != null && safeSpot.preview != null)
            event.rotationYaw = safeSpot.preview.rotation.x;
    }

    @EventHandler(priority = 1) public void onTick(PreClientTickEvent event) {
        if (!isActive()) return;
        if (mc.thePlayer != null) applyRotation();
    }

    @EventHandler(priority = -10) public void onMovementInput(MovementInputEvent event) {
        if (!isActive()) return;
        if (!shouldHandle(event)) { safeSpot = null; return; }
        boolean ladder = mc.thePlayer.isOnLadder();
        boolean liquid = mc.thePlayer.inWater;
        if (liquid || ladder) {
            module.liquidCheckCooldown.timer.reset();
            module.liquidCheckCooldown.durationMs = 1000L;
        }
        float[] input = event.getMovement();
        if (input[0] == 0.0F && input[1] == 0.0F && safeSpot == null && mc.thePlayer.motionX == 0.0D
                && mc.thePlayer.motionZ == 0.0D && mc.thePlayer.onGround && !MovementUtil.hasMoved() && !liquid && !ladder) return;
        if (safeSpot != null) {
            safeSpot.remainingTicks = Math.max(0, safeSpot.remainingTicks - 1);
            if (safeSpot.remainingTicks != 0) {
                Simulation simulation = new Simulation(mc.thePlayer);
                simulation.setRotation(new float[] {safeSpot.input.x, safeSpot.input.y}, safeSpot.jump);
                applyMovement(safeSpot, event);
                applyRotation();
                if (liquid && shouldSwimUp()) event.jump = true;
                return;
            }
            safeSpot = null;
        }
        Simulation simulation = new Simulation();
        if (findRecovery(simulation, event, event.originalJump, true)
                || findRecovery(simulation, event, !event.originalJump, false)) {
            failureTimer.reset();
        } else if (blockClutch.isEnabled()) {
            ScaffoldModule.INSTANCE.enableTemporarily();
        } else if (!failureTimer.hasElapsed(150L)) {
            com.adjustclient.ui.hud.notification.NotificationBuilder notification =
                    new com.adjustclient.ui.hud.notification.NotificationBuilder();
            notification.title = module.getName();
            notification.addText("Unable to find a safe spot for your current position. Sorry :(", (int) 4290690237L).show();
            failureTimer.lastMs = 0L;
        }
        if (liquid && shouldSwimUp()) event.jump = true;
    }

    private boolean shouldSwimUp() {
        if (mc.thePlayer.motionY < -0.18D) return true;
        Vec3 below = mc.thePlayer.getPositionVector().addVector(0.0D, -1.0D, 0.0D);
        return !BlockUtil.isInLiquidOrClimbable(new AxisAlignedBB(
                below.xCoord, below.yCoord, below.zCoord,
                below.xCoord + 1.0D, below.yCoord + 1.0D, below.zCoord + 1.0D));
    }

    @EventHandler public void onRender3D(Render3DEvent event) {
        if (!isActive()) return;
        if (safeSpot != null && safeSpot.preview != null && safeSpot.preview.entity != null) {
            EntityPlayer player = safeSpot.preview.entity;
            renderer.render(player, player.posX, player.posY, player.posZ, player.rotationYaw, event.partialTicks);
        }
    }

    private boolean hasLiquidColumn(Vec3 position) {
        if (position.yCoord < 0.0D) return false;
        float halfWidth = mc.thePlayer.width * collisionWidth;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = MathHelper.floor_double(position.xCoord - halfWidth);
                x < MathHelper.floor_double(position.xCoord + halfWidth + 1.0D); ++x) {
            for (int z = MathHelper.floor_double(position.zCoord - halfWidth);
                    z < MathHelper.floor_double(position.zCoord + halfWidth + 1.0D); ++z) {
                for (int y = MathHelper.floor_double(position.yCoord + 1.0D) - 1; y >= -1; --y) {
                    pos.set(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    if (WATER_BLOCK_PREDICATE.test(state)) return true;
                    Block block = state.getBlock();
                    if (block instanceof BlockAir) continue;
                    AxisAlignedBB collision = block.getCollisionBoundingBox(mc.theWorld, pos, state);
                    if (collision != null && collision.maxY - collision.minY != 0.0D) break;
                }
            }
        }
        return false;
    }

    private boolean withinFallLimit(Simulation simulation, Float fallLimit) {
        if (fallLimit == null) return true;
        if (simulation.player.fallDistance > fallLimit) return false;
        if (simulation.player.onGround) return true;
        Simulation stopped = new Simulation(simulation.player);
        stopped.setRotation(new float[] {0.0F, 0.0F}, false);
        stopped.player.motionX = stopped.player.motionZ = 0.0D;
        for (int tick = 0; tick < 60; ++tick) {
            if (stopped.player.fallDistance > fallLimit) return false;
            if (stopped.player.onGround) return true;
            stopped.player.tick();
        }
        return false;
    }

    private int simulatePath(Simulation simulation, int ticks, boolean landingCheck, boolean[] belowJumpLimit) {
        float jumpLimit = belowJumpLimit != null ? WorldUtil.safeFallDistance(mc.thePlayer) : 0.0F;
        for (int remaining = ticks; ; --remaining) {
            SimulationPlayer player = simulation.player;
            if (belowJumpLimit != null && player.fallDistance > jumpLimit) belowJumpLimit[0] = false;
            if (!landingCheck) {
                Vec3 position = player.getPositionVector();
                if (module.isOverVoid(position) && !hasLiquidColumn(position)) return ticks - remaining;
            }
            if (remaining < 1) return landingCheck && isUnsafeColumn(player.getPositionVector()) ? ticks - remaining : -1;
            player.tick();
        }
    }

    private boolean findRecovery(Simulation simulation, MovementInputEvent event, boolean jump, boolean tryDelayedJump) {
        float[] input = event.getMovement();
        simulation.simulateTick(mc.thePlayer, true);
        simulation.setRotation(input, jump);
        boolean ladder = mc.thePlayer.isOnLadder();
        int lookahead = ladder ? 2 : 7;
        if (mc.thePlayer.onGround && !MovementUtil.isJumping()) lookahead = 3;
        if (simulatePath(simulation, lookahead, false, null) == -1) return true;
        timer.reset();
        List<Integer> delays = null;
        if (tryDelayedJump && safeSpot == null && !mc.thePlayer.inWater) {
            delays = new ArrayList<>(Arrays.asList(-1, 0, 1, 2, 3));
            if (event.originalJump) delays.remove(Integer.valueOf(-1));
            for (int delay : delays) {
                if ((delay < 0 || mc.thePlayer.onGround) && findDelayedJump(simulation, event, delay, getFallLimit())) return true;
            }
        }
        float preferredDirection = MovementUtil.getDirection(mc.thePlayer.rotationYaw, input[0], input[1]);
        List<ClutchPlacement> candidates = new ArrayList<>();
        Function<ClutchPlacement, Boolean> collect = candidate -> {
            return candidates.add(candidate);
        };
        generateCandidates(collect, input, jump, simulation, mc.thePlayer, lookahead, preferredDirection);
        double distance = MathHelper.clamp_double(Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ), 0.3D, 1.0D);
        double radians = preferredDirection * 0.0174532925199433D;
        Vec3 currentPosition = mc.thePlayer.getPositionVector();
        Vec3 behind = currentPosition.addVector(Math.sin(radians) * distance, 0.0D, -Math.cos(radians) * distance);
        java.util.function.ToDoubleFunction<ClutchPlacement> rank = candidate -> {
            Vec3 origin = ladder ? currentPosition : behind;
            double dx = origin.xCoord - candidate.position.xCoord;
            double dz = origin.zCoord - candidate.position.zCoord;
            double score = Math.sqrt(dx * dx + dz * dz);
            double drop = currentPosition.yCoord - candidate.position.yCoord;
            if (drop > 0.0D) score -= drop;
            return ladder ? score : -score;
        };
        Comparator<ClutchPlacement> comparator = Comparator.comparingDouble(rank);
        if (getFallLimit() != null) {
            comparator = Comparator.comparing((ClutchPlacement candidate) -> {
                return candidate.withinFallLimit;
            }).reversed().thenComparing(comparator);
        }
        candidates.sort(comparator);
        if (getFallLimit() != null && (candidates.isEmpty() || !candidates.get(0).withinFallLimit) && delays != null) {
            for (int delay : delays) {
                if ((delay < 0 || mc.thePlayer.onGround) && findDelayedJump(simulation, event, delay, getFallLimit())) return true;
            }
        }
        if (candidates.isEmpty()) return false;
        safeSpot = candidates.get(0);
        applyMovement(safeSpot, event);
        return true;
    }

    private boolean findDelayedJump(Simulation simulation, MovementInputEvent event, int jumpDelay, Float fallLimit) {
        if (event.sneak) return false;
        simulation.simulateTick(mc.thePlayer, false);
        float[] input = event.getMovement();
        simulation.setRotation(input, false);
        for (int i = 0; i < jumpDelay; ++i) simulation.player.tick();
        simulation.player.movementInput.jump = jumpDelay >= 0;
        Vec3 originalPosition = mc.thePlayer.getPositionVector();
        boolean fullDepth = module.isFullVoidDistance();
        double range = (module.voidDistOverrideKey.isDown() ? module.voidDistOverride : module.voidDistance).getDouble();
        double lowestDeltaY = -(range - 0.6D);
        boolean belowFallLimit = true;
        boolean crossedVoid = false;
        int safeAttempts = 0;
        Function<ClutchPlacement, Boolean> anyCandidate = candidate -> {
            return false;
        };
        for (int tick = 0; tick < 35; ++tick) {
            SimulationPlayer player = simulation.player;
            Vec3 position = player.getPositionVector();
            if (fallLimit != null && player.fallDistance > fallLimit) belowFallLimit = false;
            if (!fullDepth && position.yCoord - originalPosition.yCoord <= lowestDeltaY) return false;
            int nextAttempts = 0;
            if (crossedVoid && (!module.isOverVoid(position) || hasLiquidColumn(position))) {
                double dx = originalPosition.xCoord - position.xCoord;
                double dz = originalPosition.zCoord - position.zCoord;
                if (dx * dx + dz * dz > 0.25D) {
                    boolean withinLimit = hasLiquidColumn(position)
                            || belowFallLimit && withinFallLimit(simulation, fallLimit);
                    nextAttempts = safeAttempts + 1;
                    if (fallLimit == null || withinLimit) {
                        Simulation alternatives = new Simulation();
                        boolean alternative = generateCandidates(anyCandidate, input, false, alternatives, player,
                                player.onGround ? 2 : 4, null);
                        if (alternative) {
                            boolean staysAboveFloor = true;
                            if (!fullDepth) {
                                alternatives.simulateTick(player, false);
                                for (int step = 0; step < 10; ++step) {
                                    alternatives.player.tick();
                                    if (alternatives.player.posY - originalPosition.yCoord <= lowestDeltaY) {
                                        staysAboveFloor = false;
                                        break;
                                    }
                                    if (alternatives.player.onGround) break;
                                }
                            }
                            if (!staysAboveFloor) return false;
                            LegitSafeSpot preview = new LegitSafeSpot(new Vec2f(player.rotationYaw, player.rotationPitch), player.isSprinting());
                            if (displayFreeze.isEnabled()) preview.capture(player);
                            int duration = jumpDelay - 1 + tick;
                            safeSpot = new ClutchPlacement(new Vec2f(input[0], input[1]), position, preview,
                                    duration, jumpDelay, jumpDelay >= 0, withinLimit);
                            applyMovement(safeSpot, event);
                            return true;
                        }
                        if (safeAttempts >= 3) return false;
                    }
                }
            }
            if (tick > 0 && !crossedVoid && isUnsafeColumn(position)) crossedVoid = true;
            if (tick >= 6 && !crossedVoid) return false;
            player.tick();
            player.movementInput.jump = false;
            safeAttempts = nextAttempts;
        }
        return false;
    }

    private boolean generateCandidates(Function<ClutchPlacement, Boolean> callback, float[] input, boolean jump,
                                       Simulation simulation, EntityPlayer source, int horizon, Float preferredDirection) {
        List<Vec2f> directions = new ArrayList<>();
        for (float forward = -1.0F; forward <= 1.0F; forward += 1.0F) {
            for (float strafe = -1.0F; strafe <= 1.0F; strafe += 1.0F) {
                if (forward == input[0] && strafe == input[1]) continue;
                directions.add(new Vec2f(forward, strafe));
            }
        }
        if (preferredDirection != null) directions.sort(Comparator.comparingDouble(direction -> {
            if (direction.x == 0.0F && direction.y == 0.0F) return Double.NEGATIVE_INFINITY;
            float yaw = MovementUtil.getDirection(mc.thePlayer.rotationYaw, direction.x, direction.y);
            return -Math.abs(MathHelper.wrapAngleTo180_float(preferredDirection - yaw));
        }));
        for (Vec2f direction : directions) {
            simulation.simulateTick(source, true);
            simulation.setRotation(new float[] {direction.x, direction.y}, jump);
            boolean[] belowJumpLimit = {true};
            if (simulatePath(simulation, horizon + 1, true, belowJumpLimit) != -1) continue;
            Vec3 position = simulation.player.getPositionVector();
            boolean withinLimit = hasLiquidColumn(position) || belowJumpLimit[0] && withinFallLimit(simulation, getFallLimit());
            ClutchPlacement candidate = new ClutchPlacement(direction, position, null, -1, 0, jump, withinLimit);
            if (!callback.apply(candidate)) return true;
        }
        return false;
    }

    private boolean shouldHandle(MovementInputEvent event) {
        if (ScaffoldModule.INSTANCE.isEnabled()) {
            if (Boolean.TRUE.equals(ScaffoldModule.INSTANCE.placeReady)
                    || ScaffoldModule.INSTANCE.field137 || !AntiVoidModule.isWorldReady()) return false;
        }
        if (!event.jump && mc.thePlayer.onGround
                && (event.sneak || SafeWalkModule.INSTANCE.isActive())) return false;
        if ((!mc.thePlayer.onGround || event.jump) && SafeWalkModule.INSTANCE.isActive()
                && SafeWalkModule.INSTANCE.isEagleActive()) return false;
        return !module.shouldIgnore();
    }

    private boolean isUnsafeColumn(Vec3 position) {
        boolean clear;
        float halfWidth = mc.thePlayer.width * module.mode.getValue().collisionWidth;
        if (module.liquidCheckCooldown.isExpired()) {
            clear = WorldUtil.isColumnCollisionFree(position, module.getCheckBottom(position.yCoord), halfWidth);
        } else {
            double bottom = module.isFullVoidDistance() ? 0.0D : Math.max(0.0D, module.getCheckBottom(position.yCoord));
            clear = WorldUtil.isColumnFreeOfLiquidAndClimbables(position, bottom, halfWidth);
        }
        return clear && !hasLiquidColumn(position);
    }

    private void applyMovement(ClutchPlacement candidate, MovementInputEvent event) {
        event.movementOwners.add(AntiVoidModule.class);
        event.back = candidate.input.x < 0.0F;
        event.forward = candidate.input.x > 0.0F;
        event.left = candidate.input.y > 0.0F;
        event.right = candidate.input.y < 0.0F;
        event.sneak = false;
        event.jump = safeSpot.jump && safeSpot.jumpDelay <= safeSpot.initialTicks - safeSpot.remainingTicks;
    }

    private Float getFallLimit() {
        return maxFallDist.getInt() == maxFallDist.getMax().intValue() ? null : Float.valueOf(maxFallDist.getInt());
    }

    private void applyRotation() {
        if (safeSpot != null && safeSpot.preview != null && MovementFixModule.INSTANCE.isEnabled()) {
            RotationHandler.INSTANCE.target = safeSpot.preview.rotation;
            RotationHandler.INSTANCE.model = InstantRotationModel.INSTANCE;
            RotationHandler.INSTANCE.requested = true;
            RotationHandler.INSTANCE.active = true;
            RotationHandler.release();
        }
    }
}
