package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.Adjust;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.GameLoopEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public final class HypixelScaffoldMode extends ScaffoldMode {

    public static HypixelScaffoldMode INSTANCE;

    private static final float[] BACK_WEIGHTS = new float[]{0.17F, 0.66F, 0.17F};
    private static final float[] FORWARD_WEIGHTS = new float[]{0.4F, 0.6F};

    public enum Stage {
        NONE,
        GROUND,
        FORWARD,
        BACK,
        RETURN
    }

    private Stage stage = Stage.NONE;
    private BlockPos lastGroundSupport;
    private int stageTicks;
    private int forwardTicks;

    public HypixelScaffoldMode() {
        INSTANCE = this;
    }

    public boolean shouldAutoJump() {
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        this.stage = Stage.NONE;
        this.stageTicks = 0;
        this.forwardTicks = 0;
        this.lastGroundSupport = null;
    }

    private Integer predictLandingTick() {
        int targetY = this.module.baseY;
        if (!this.module.noAscendActive) {
            targetY++;
        }
        Simulation sim = new Simulation(mc.thePlayer);
        for (int tick = 1; tick <= 20; tick++) {
            sim.player.tick();
            double y = sim.player.posY == -0.0D ? 0.0D : sim.player.posY;
            if (sim.player.onGround) {
                return y >= (double) targetY - 0.01D ? Integer.valueOf(tick) : null;
            }
            if ((double) targetY - 1.0D > y) {
                return null;
            }
        }
        return null;
    }

    private boolean willStepOffGround() {
        Simulation sim = new Simulation();
        sim.simulateTick(mc.thePlayer, true);
        sim.player.movementInput.jump = false;
        sim.player.tick();
        return !sim.player.onGround;
    }

    @Override
    public boolean isAboveBuildLimit(int y) {
        Integer limit = (Integer) Adjust.gameStateTracker.state.getAdditionalData();
        if (limit == null) {
            return false;
        }
        return limit.intValue() <= y;
    }

    @EventHandler
    public void onControllerUpdate(GameLoopEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (e.preScheduled) {
            if (this.towering) {
                return;
            }
            this.module.releaseInput();
        }
    }

    @EventHandler(priority = 1)
    public void onMotionPre(MotionEvent e) {
        if (!this.isActive() || !e.pre) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (!player.onGround && player.offGroundTicks < 11) {
            return;
        }
        this.towering = false;
    }

    @EventHandler
    public void onMotion(MoveFlyingEvent e) {
        if (!this.isActive() || e.pre) {
            return;
        }
        if (!this.module.settings.isTower()) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.offGroundTicks != 6) {
            return;
        }
        if (ServerTracker.INSTANCE.ticksSinceTabUpdate != 0) {
            return;
        }
        BlockPos below = new BlockPos(player.posX, player.posY - 2.0D, player.posZ);
        Block block = mc.theWorld.getBlockState(below).getBlock();
        if (block.isReplaceable(mc.theWorld, below)) {
            return;
        }
        double reduction = 0.25D - ServiceUtil.movementAdjustment * (double) 0.001F;
        if (ServiceUtil.isActiveModel(ServerType.HYPIXEL)) {
            player.motionY = player.motionY - reduction;
        }
        this.towering = true;
    }

    @EventHandler
    public void onMoveInput(MovementInputEvent e) {
        if (!this.isActive()) {
            return;
        }
        if (e.jump && mc.thePlayer.onGround && this.stage == Stage.GROUND && this.module.shouldRotateForward(false)) {
            RotationHandler rotationHandler = RotationHandler.INSTANCE;
            float targetYaw = rotationHandler.target != null ? rotationHandler.target.x : mc.thePlayer.rotationYaw;
            boolean serverAligned;
            if (this.forwardTicks >= 2) {
                serverAligned = true;
            } else {
                float serverYaw = ServerTracker.INSTANCE.rotation != null ? ServerTracker.INSTANCE.rotation.x : 0.0F;
                serverAligned = Math.abs(MathHelper.wrapAngleTo180_float(serverYaw - targetYaw)) < 2.0F;
            }
            if (rotationHandler.active && rotationHandler.requested
                    && (!serverAligned || Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - targetYaw)) >= 2.0F)
                    && !this.willStepOffGround()) {
                e.jump = false;
            }
        }
        if (this.towering) {
            e.back = false;
            e.forward = false;
            e.left = false;
            e.right = false;
        }
    }

    @Override
    public void onPlace(PlacementTarget placement) {
        int remaining = Adjust.placementLimiter.placements;
        if (remaining >= 1) {
            Adjust.placementLimiter.placements = remaining - 1;
        }
    }

    @Override
    public boolean canSprint() {
        return this.module.settings.isAllowSprint() && MovementFixModule.INSTANCE.isEnabled();
    }

    @Override
    public void onPostRotation() {
        this.stageTicks++;
        if (this.stage != Stage.BACK && this.stage != Stage.NONE) {
            this.forwardTicks++;
        }
    }

    @Override
    public boolean canPlaceInGame() {
        if (ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL) {
            HypixelGameState game = Adjust.gameStateTracker.state;
            if (game.isSpectator()) {
                return false;
            }
            if (game.isSpecialState() != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canNoAscend() {
        return !mc.thePlayer.isPotionActive(Potion.moveSpeed);
    }

    @Override
    public boolean canAutoJump() {
        return ServerTracker.INSTANCE.ticks < 1;
    }

    @Override
    public boolean allowForwardTurn() {
        if (!this.module.settings.isAscendOnJump() && mc.gameSettings.keyBindJump.pressed) {
            return false;
        }
        if (!this.module.settings.isSnap()) {
            return true;
        }
        float direction = Math.abs(MovementUtil.getDirection() % 90.0F);
        return direction <= 5.0F || direction >= 85.0F;
    }

    @Override
    public boolean shouldLookForward() {
        if (!this.module.shouldRotateForward(false)) {
            this.stage = Stage.NONE;
            this.stageTicks = 0;
            this.forwardTicks = 0;
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.onGround) {
            if (this.stage != Stage.GROUND) {
                this.stage = Stage.GROUND;
                this.stageTicks = 0;
            }
            BlockPos below = new BlockPos(MathHelper.floor_double(player.posX),
                    MathHelper.floor_double(player.posY - 1.0D),
                    MathHelper.floor_double(player.posZ));
            Block block = mc.theWorld.getBlockState(below).getBlock();
            if (!block.isReplaceable(mc.theWorld, below)) {
                this.lastGroundSupport = below;
            }
            return false;
        }
        switch (this.stage) {
            case BACK:
                if (this.predictLandingTick() != null) {
                    this.stage = Stage.RETURN;
                    this.stageTicks = 0;
                    return true;
                }
                return false;
            case RETURN:
                if (this.predictLandingTick() == null) {
                    this.stage = Stage.BACK;
                    this.stageTicks = 0;
                    this.forwardTicks = 0;
                    return false;
                }
                return true;
            case NONE:
            case GROUND:
            case FORWARD:
                boolean outOfReach = !this.module.noAscendActive;
                if (this.module.noAscendActive) {
                    BlockPos lastPlaced = this.module.lastPlacement != null ? this.module.lastPlacement.placedPos : null;
                    int baseY = this.module.baseY;
                    double reach = mc.playerController.getBlockReachDistance();
                    Simulation sim = new Simulation(player);
                    Set<BlockPos> airColumns = new HashSet<>();
                    int simTicks = 0;
                    while (simTicks < 20) {
                        sim.player.tick();
                        simTicks++;
                        Vec3 pos = new Vec3(sim.player.posX, sim.player.posY, sim.player.posZ);
                        if (simTicks == 3) {
                            Vec3 eyes = new Vec3(pos.xCoord + 0.0D,
                                    pos.yCoord + (double) mc.thePlayer.getEyeHeight(),
                                    pos.zCoord + 0.0D);
                            double minDistance = Double.MAX_VALUE;
                            if (lastPlaced != null) {
                                minDistance = new Vec3(lastPlaced).addVector(0.5D, 0.5D, 0.5D).distanceTo(eyes);
                            }
                            if (this.lastGroundSupport != null) {
                                minDistance = Math.min(minDistance,
                                        new Vec3(this.lastGroundSupport).addVector(0.5D, 0.5D, 0.5D).distanceTo(eyes));
                            }
                            if (minDistance != Double.MAX_VALUE && minDistance > reach - 0.5D) {
                                outOfReach = true;
                                break;
                            }
                        }
                        if (sim.player.onGround || pos.yCoord < (double) baseY) {
                            break;
                        }
                        for (double dz = -0.3D; dz <= 0.3D; dz += 0.6D) {
                            for (double dx = -0.3D; dx <= 0.3D; dx += 0.6D) {
                                BlockPos check = new BlockPos(pos.xCoord + dx, (double) (baseY - 1), pos.zCoord + dz);
                                Block block = BlockUtil.getBlock(check);
                                if (block.isReplaceable(mc.theWorld, check)) {
                                    airColumns.add(check);
                                }
                            }
                        }
                    }
                    if (simTicks > airColumns.size() + 5 && !outOfReach) {
                        this.stage = Stage.FORWARD;
                        return true;
                    }
                }
                this.stage = Stage.BACK;
                this.stageTicks = 0;
                this.forwardTicks = 0;
                return false;
            default:
                return false;
        }
    }

    @Override
    public RotationModel getForwardRotationModel() {
        if (mc.thePlayer.onGround && this.willStepOffGround()) {
            return InstantRotationModel.INSTANCE;
        }
        int remaining = 2 - this.forwardTicks;
        if (remaining >= 2) {
            return new ScaffoldWeightedRotationModel(FORWARD_WEIGHTS, remaining);
        }
        return InstantRotationModel.INSTANCE;
    }

    @Override
    public RotationModel getPlaceRotationModel() {
        if (this.stage == Stage.BACK && this.stageTicks < 3 && !mc.thePlayer.onGround) {
            return new ScaffoldWeightedRotationModel(BACK_WEIGHTS, 3 - this.stageTicks);
        }
        return null;
    }

    @Override
    public boolean canSnap() {
        return false;
    }

    @Override
    public boolean useFastRotations() {
        return ServiceUtil.isActiveModel(ServerType.HYPIXEL) && MovementFixModule.INSTANCE.isEnabled();
    }

    @Override
    public boolean forceRaycast() {
        return true;
    }

    @Override
    public boolean isTowerLowOnly() {
        return true;
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent e) {
        if (e.isCancelled() || !this.isActive() || e.outgoing) {
            return;
        }
        Packet<?> packet = e.packet;
        if (this.towering) {
            if (ServiceUtil.isActiveModel(ServerType.HYPIXEL)
                    && ServerTracker.INSTANCE.serverState.antiCheatModel.isAllChecksPassed()) {
                if (packet instanceof S27PacketExplosion) {
                    S27PacketExplosion explosion = (S27PacketExplosion) packet;
                    if (explosion.func_149149_c() == 0.0F && explosion.func_149144_d() == 0.0F
                            && explosion.func_149147_e() == 0.0F) {
                        return;
                    }
                    this.module.holdInput();
                } else if (packet instanceof S12PacketEntityVelocity) {
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        this.module.holdInput();
                    }
                }
                return;
            }
        }
        if (packet instanceof S08PacketPlayerPosLook || packet instanceof S01PacketJoinGame) {
            this.module.releaseInput();
        }
    }
}
