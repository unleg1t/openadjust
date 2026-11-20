package com.adjustclient.modules.impl.movement.step;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.StepAttemptEvent;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.JumpEvent;
import com.adjustclient.core.events.MotionEvent;
import com.adjustclient.core.events.MoveEvent;
import com.adjustclient.core.events.ItemPlacementAttemptEvent;
import com.adjustclient.core.events.CancellablePlayerUpdateEvent;
import com.adjustclient.modules.impl.player.breaker.BreakerModule;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.RotationUtil;
import com.adjustclient.utilities.Simulation;
import com.adjustclient.utilities.SimulationPlayer;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.WorldUtil;
import com.adjustclient.utilities.prediction.MotionPredictor;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MotionStepMode extends StepMode {
    public static MotionStepMode INSTANCE;
    StepResult result;
    private final Timer stepTimer = new Timer(0L);
    private float movementTicks;
    int cooldownTicks;
    int lastAlignedTick = -1;
    private int recentSteps;

    public MotionStepMode(StepModule module) {
        super(module, "Motion");
        INSTANCE = this;
    }

    @EventHandler public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isActive()) return;
        if (!MovementUtil.isMoving()) movementTicks = 0.0F;
        if (cooldownTicks >= 1) {
            --cooldownTicks;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) return;
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            return;
        }
        if (!mc.thePlayer.onGround || module.disableBind.isDown() || !MovementUtil.isMoving()
                || mc.thePlayer.isPotionActive(Potion.jump) || mc.currentScreen instanceof GuiContainer
                || Adjust.movementPredictor.historyTicks < 10 || result != null
                || MovementUtil.isMovementRestricted(mc.thePlayer) || WorldUtil.isLookingAtBlock(false)) return;
        double stepHeight = 1.0D - mc.thePlayer.lastReportedPosY % 0.015625D;
        recentSteps = stepTimer.hasElapsed(500L) ? 0 : recentSteps + 1;
        MotionPredictor.getPredictionAntiCheat();
        movementTicks += 0.4F;
        Simulation simulation = new Simulation(mc.thePlayer);
        SimulationPlayer player = simulation.player;
        for (int i = 0; i < 3; ++i) {
            AxisAlignedBB box = player.getEntityBoundingBox();
            AxisAlignedBB ahead = new AxisAlignedBB(box.minX - 0.00625D, box.minY, box.minZ - 0.00625D,
                    box.maxX + 0.00625D, box.maxY, box.maxZ + 0.00625D)
                    .offset(player.motionX, stepHeight, player.motionZ);
            for (AxisAlignedBB obstacle : mc.theWorld.getCollidingBoundingBoxes(player, ahead, true, false)) {
                IBlockState state = mc.theWorld.getBlockState(new BlockPos(obstacle.minX, obstacle.minY, obstacle.minZ));
                Block block = state.getBlock();
                if (block instanceof BlockSnow || block instanceof BlockHopper) continue;
                Vec3 center = new Vec3((obstacle.minX + obstacle.maxX) / 2.0D,
                        (obstacle.minY + obstacle.maxY) / 2.0D, (obstacle.minZ + obstacle.maxZ) / 2.0D);
                Vec3 position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                if (block instanceof BlockStairs
                        && state.getValue(BlockStairs.HALF) == (BlockStairs.EnumHalf) BlockStairs.EnumHalf.BOTTOM
                        && EnumFacing.fromAngle(RotationUtil.getYawBetween(center, position).x).getOpposite()
                        != state.getValue(BlockStairs.FACING)) continue;
                if (movementTicks < 3.0F) {
                    BlockPos adjacent = new BlockPos(obstacle.minX, obstacle.minY, obstacle.minZ)
                            .offset((EnumFacing) EnumFacing.DOWN).offset(EnumFacing.fromAngle(RotationUtil.getYawBetween(position, center).x));
                    if (!BlockUtil.hasCollision(adjacent) && !BlockUtil.hasCollision(adjacent.offset((EnumFacing) EnumFacing.DOWN))
                            && !WorldUtil.isFreeAt(new Vec3(adjacent.getX() + 0.5D, adjacent.getY() + 0.5D,
                                    adjacent.getZ() + 0.5D), 0.0D)) continue;
                }
                double height = obstacle.maxY - mc.thePlayer.posY;
                if (height != stepHeight) continue;
                AxisAlignedBB above = BlockUtil.getCollisionBox(new BlockPos(obstacle.minX,
                        mc.thePlayer.posY + height, obstacle.minZ));
                if (above != null && above.maxY - above.minY > 0.0D
                        && mc.thePlayer.posY - (int) mc.thePlayer.posY != above.maxY - above.minY
                        && (above.maxX - above.minX >= 1.0D
                        || above.maxX - above.minX >= obstacle.maxX - obstacle.minX
                        || above.maxY - above.minY <= 0.1D)) continue;
                StepAttemptEvent attempt = new StepAttemptEvent(position, position.addVector(0.0D, height, 0.0D));
                EventBus.post(attempt);
                if (attempt.cancelled) continue;
                result = searchStep(obstacle, height);
                if (result != null && MovementUtil.isAutoJumping()) result = null;
                if (result != null) stepTimer.reset();
                cooldownTicks = 2;
                return;
            }
            player.tick();
        }
    }

    @EventHandler public void onMotion(MotionEvent event) {
        if (isActive() && event.pre && !event.onGround && event.y % 0.015625D == 0.0D
                && lastAlignedTick == mc.thePlayer.ticksExisted && result != null && result.index == 3) {
            event.onGround = true;
            lastAlignedTick = -1;
        }
    }

    @EventHandler public void onPacketReceive(com.adjustclient.core.events.IncomingVelocityEvent event) {
        if (!event.isCancelled() && isActive() && event.packet instanceof S12PacketEntityVelocity velocity
                && mc.thePlayer != null && velocity.getEntityID() == mc.thePlayer.getEntityId()
                && (double) velocity.getMotionY() / 8000.0D != 0.0D) {
            result = null;
            lastAlignedTick = -1;
        }
    }

    public StepResult searchStep(AxisAlignedBB obstacle, double height) {
        Vec3 center = new Vec3((obstacle.minX + obstacle.maxX) / 2.0D,
                (obstacle.minY + obstacle.maxY) / 2.0D, (obstacle.minZ + obstacle.maxZ) / 2.0D);
        float obstacleYaw = RotationUtil.getYawBetween(center,
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).x;
        Simulation simulation = new Simulation();
        simulation.simulateTick(mc.thePlayer, true);
        SimulationPlayer player = simulation.player;
        float movementYaw = RandomUtil.jitter(MovementUtil.getDirection(), 1.0F);
        double baseSpeed = Adjust.movementPredictor.historyTicks >= 51 ? 0.2D : 0.19D;
        double speed = MovementUtil.getSpeedWithPotion(RandomUtil.jitter(baseSpeed, 0.003D), 0.19D) * 0.969D;
        double radians = Math.toRadians(obstacleYaw);
        player.motionX = -Math.sin(radians) * speed;
        player.motionZ = Math.cos(radians) * speed;
        player.movementInput.moveForward = 1.0F;
        player.movementInput.moveStrafe = 0.0F;
        player.prevRotationYaw = obstacleYaw;
        player.rotationYaw = obstacleYaw;
        player.jump();
        double targetY = mc.thePlayer.posY + height;
        AtomicReference<Vec3> motion = new AtomicReference<>();
        player.afterMovement = ignored -> {
            SimulationPlayer current = simulation.player;
            motion.set(new Vec3(current.motionX, current.motionY, current.motionZ));
        };
        AxisAlignedBB[] boxes = new AxisAlignedBB[4];
        Vec3[] motions = new Vec3[4];
        boolean reachedGround = false;
        for (int tick = 0; tick < 6; ++tick) {
            if (tick < 4) {
                boxes[tick] = player.getEntityBoundingBox();
                motions[tick] = new Vec3(player.motionX, player.motionY, player.motionZ);
            }
            player.tick();
            if (tick >= 5) {
                if (!player.onGround || (player.posY > targetY
                        && player.posY - targetY % 0.015625D != 0.0D)) return null;
            } else {
                int airTicks = player.offGroundTicks;
                if (airTicks == 0) {
                    if (tick == 4) reachedGround = true;
                } else if (airTicks == 2) {
                    player.motionY = 1.0D - player.posY % 1.0D;
                    if (mc.thePlayer.posY == (int) mc.thePlayer.posY + 0.5D) player.motionY -= 0.5D;
                    if (player.motionY > 0.3D) return null;
                } else if (airTicks == 3) {
                    if (player.posY != targetY) return null;
                    player.motionY = (double) -0.0784F;
                    double direction = Math.toRadians(movementYaw);
                    player.motionX = -Math.sin(direction) * speed;
                    player.motionZ = Math.cos(direction) * speed;
                }
            }
        }
        return reachedGround ? new StepResult(motions, boxes) : null;
    }

    @EventHandler public void onMove(MoveEvent event) {
        if (!isActive() || result == null) return;
        AxisAlignedBB last = result.boxes[result.boxes.length - 1];
        AxisAlignedBB support = last.addCoord(0.0D, -0.0125D, 0.0D);
        if (BlockUtil.isColliding(support) && !(mc.currentScreen instanceof GuiContainer)) {
            cooldownTicks = 2;
            int index = result.index;
            Vec3 next = result.motions[index];
            mc.thePlayer.motionX = next.xCoord;
            mc.thePlayer.motionY = next.yCoord;
            mc.thePlayer.motionZ = next.zCoord;
            mc.thePlayer.setEntityBoundingBox(result.boxes[index]);
            event.x = next.xCoord;
            event.y = next.yCoord;
            event.z = next.zCoord;
            if (next.yCoord > 0.0D) lastAlignedTick = mc.thePlayer.ticksExisted;
            if (++result.index > result.motions.length - 1) {
                mc.thePlayer.onGround = true;
                mc.thePlayer.offGroundTicks = 0;
                result = null;
            }
        } else {
            result = null;
            lastAlignedTick = -1;
        }
    }
}
