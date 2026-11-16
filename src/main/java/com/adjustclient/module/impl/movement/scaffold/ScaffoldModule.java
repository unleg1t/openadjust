package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.event.EventBus;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.UserInputVerificationEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.LinearRotationModel;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.rotation.RotationSetting;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.GameStateUtil;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.Random;
import java.util.function.UnaryOperator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockWall;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldSettings.GameType;
import static com.adjustclient.util.MinecraftInstance.mc;
import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Feature;
import com.adjustclient.anticheat.feature.ModuleFeature;
import com.adjustclient.game.packet.InboundPacketQueue;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.module.impl.movement.sprint.RotateOmniSprintMode;
import com.adjustclient.module.impl.player.inventory.item.type.BlockItemType;
import com.adjustclient.module.util.CpsCounter;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.PacketQueue;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import net.minecraft.init.Blocks;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.service.config.ConfigurationService;

public final class ScaffoldModule extends Module {

    public static ScaffoldModule INSTANCE;

    private PlacementPredictor placementScanner;
    private PlacementTracker placementTracker;
    public ScaffoldSettings settings;
    private SafeWalkHelper safeWalk;
    public PlacementFinder placementFinder;
    public Boolean placeReady;
    private PlacementTarget placement;
    public PlacementTarget lastPlacement;
    private BlockPos lastPlacePos;
    private BlockPos lastPlaceBox;
    private Integer candidateSlot;
    private PacketHold inputOverride;
    private PacketQueue clickQueue;
    Timer autoEnableTimer;
    public float targetYaw;
    public float targetPitch;
    private float rotationJitter;
    public int ticksSincePlace = 100;
    private int validSlotTicks;
    private int sneakTicks;
    public int baseY;
    public int field116;
    private int placeDelay;
    private boolean field124;
    private boolean rotating;
    private boolean field126;
    private boolean rotated;
    private boolean slotChanged;
    public boolean noAscendActive;
    boolean field130;
    private boolean field131;
    private boolean field132;
    private boolean field133;
    boolean towering;
    private boolean towerKeyOnGround;
    private boolean field136;
    public boolean field137;

    public ScaffoldModule() {
        super("Scaffold", Category.MOVEMENT);
        INSTANCE = this;
        this.settings = new ScaffoldSettings(this);
        this.clickQueue = new PacketQueue();
        this.notifyToggle = false;
        INSTANCE = this;
    }

    private boolean canPlaceNow() {
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (mode instanceof HypixelScaffoldMode) {
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
        }
        if (this.placementTracker.hasPending()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.itemInUse != null) {
            return false;
        }
        if (player.ridingEntity != null) {
            return false;
        }
        com.adjustclient.module.util.SwingMouseClick attackState = ((com.adjustclient.module.util.MouseClickHelper) Adjust.mouseClickHelper).left;
        if (attackState != null) {
            if (!attackState.disabled) {
                Boolean pressed = attackState.down != null ? attackState.down
                        : Boolean.valueOf(attackState.key.pressed || attackState.holdTicks >= 1);
                if (pressed) {
                    return false;
                }
            }
            if (ServerTracker.INSTANCE != null) {
                if (ServerTracker.INSTANCE.attackedEntity != null) {
                    return false;
                }
                return !ServerTracker.INSTANCE.usedItemSinceMovement;
            }
        }
        throw new NullPointerException();
    }

    private boolean isRotationWindowActive() {
        if (!this.rotating) {
            return false;
        }
        return Math.abs(getUsernameHash()) % 3 + 20 > this.ticksSincePlace;
    }

    private static int getUsernameHash() {
        return Adjust.username == null ? 0 : Adjust.username.hashCode();
    }

    /**
     * Prepares the click on the targeted block: {@link #R()} must have clicked, the held item must be a block and the
     * crosshair target is checked with {@code ItemBlock.canPlaceBlockOnSide}; then a right click is armed in the click
     * state (button 1, pressed) and the click manager is triggered.
     */
    private void prepareClick() {
        if (!this.R()) {
            return;
        }
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        if (held == null) {
            return;
        }
        if (!(held.getItem() instanceof ItemBlock)) {
            return;
        }
        MovingObjectPosition over = mc.objectMouseOver;
        if (over == null) {
            return;
        }
        if (over.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
            boolean e = mode.e();
            Block block = mc.theWorld.getBlockState(over.getBlockPos()).getBlock();
            if (BlockUtil.isInteractable(block)) {
                if (!e) {
                    return;
                }
                WorldUtil.clearMouseOver();
            } else {
                if (held.getItem().canPlaceBlockOnSide(mc.theWorld, over.getBlockPos(), over.sideHit, mc.thePlayer, held)) {
                    if (!e) {
                        return;
                    }
                    WorldUtil.clearMouseOver();
                }
            }
        }
        // arm a right click
        com.adjustclient.module.util.SwingMouseClick click = ((com.adjustclient.module.util.MouseClickHelper) Adjust.mouseClickHelper).right;
        click.clicks = Integer.valueOf(1);
        click.down = Boolean.TRUE;
        click.holdTicks = 0;
        click.swing = false;
        Adjust.cpsCounter.recordClick();  // click manager: perform pending click
    }

    /** Clicks via the click manager while inside the rotation window (see {@link #isRotationWindowActive()}). */
    private boolean R() {
        if (!this.rotating) {
            return false;
        }
        if (Math.abs(getUsernameHash()) % 3 + 20 <= this.ticksSincePlace) {
            return false;
        }
        CpsSettings cps = this.settings.cps;
        int delay = cps.clickDelay;
        CpsCounter clickManager = Adjust.cpsCounter;
        if (delay == -1) {
            cps.rollClickDelay();
            return clickManager.canClick(cps.clickDelay, CpsCounter.Button.RIGHT, 0);
        }
        return clickManager.canClick(delay, CpsCounter.Button.RIGHT, 0);
    }

    /**
     * Snap + Hypixel rotation guard: true when "Snap" is on and the mode is not Hypixel; on Hypixel only when the two
     * server timers of the network handler ran out, no placement is pending, and the movement direction is not close
     * to an axis (|dir mod 90| between 5 and 85 degrees).
     */
    private boolean S() {
        if (!this.settings.isSnap()) {
            return false;
        }
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (!(mode instanceof HypixelScaffoldMode)) {
            return true;
        }
        if (ServerTracker.INSTANCE.field116 != null) {
            if (((Timer) ServerTracker.INSTANCE.field116).lastMs + 1000L > System.currentTimeMillis()) {
                return false;
            }
            if (ServerTracker.INSTANCE.velocityTimer != null) {
                if (((Timer) ServerTracker.INSTANCE.velocityTimer).lastMs + 2000L > System.currentTimeMillis()) {
                    return false;
                }
                if (this.placementTracker.hasPending()) {
                    return false;
                }
                double direction = MovementUtil.getDirection() % 90.0D;
                double abs = Math.abs(direction);
                return abs > 5.0D && abs < 85.0D;
            }
        }
        throw new NullPointerException();
    }

    private boolean T() {
        if (this.autoEnableTimer != null) {
            return true;
        }
        if (this.field131) {
            return true;
        }
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (!(mode instanceof HypixelScaffoldMode)) {
            return false;
        }
        if (!ServiceUtil.isActiveModel(ServerType.HYPIXEL)) {
            return false;
        }
        return MovementFixModule.INSTANCE.isEnabled();
    }

    /**
     * Whether the player's box moved down by {@code dy} would touch a block while the player is falling below the
     * relevant base level (feet floor, or {@link #baseY} while No ascend is active).
     */
    private boolean willLandOnBlock(double dy) {
        EntityPlayerSP player = mc.thePlayer;
        if (player.motionY >= 0.0D) {
            return false;
        }
        if (player.onGround) {
            return false;
        }
        double posY = player.posY;
        int level = this.noAscendActive ? this.baseY : (int) posY;
        if ((double) level <= posY + dy) {
            return false;
        }
        AxisAlignedBB box = player.getEntityBoundingBox().addCoord(0.0D, dy, 0.0D);
        return BlockUtil.isColliding(box);
    }

    @com.adjustclient.event.EventHandler(priority = 62)
    public void onPreUpdate(com.adjustclient.event.IngameActionEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        if (e.suppressActions) {
            this.placement = null;
            return;
        }
        PlacementTarget current = this.placement;
        if (current != null) {
            EntityPlayerSP player = mc.thePlayer;
            MovingObjectPosition hit = WorldUtil.rayTraceBlock(current.pos, player,
                    mc.playerController.getBlockReachDistance(), player.rotationYaw, player.rotationPitch);
            if (hit != null && current.facing == hit.sideHit) {
                this.placement = new PlacementTarget(current.pos, current.facing, hit.hitVec,
                        new Vec2f(player.rotationYaw, player.rotationPitch), current.yaw);
            } else if (this.placementFinder.useRaycast()) {
                this.placement = null;
            }
        }
        com.adjustclient.module.util.SwingMouseClick clickState = ((com.adjustclient.module.util.MouseClickHelper) Adjust.mouseClickHelper).right;
        if (this.placeReady != null && this.placeReady) {
            if (this.placement != null) {
                this.lastPlacePos = this.placement.pos;
                this.lastPlaceBox = this.placement.placedPos;
                this.handlePlacement(this.placement);
            } else {
                this.prepareClick();
            }
            // keep the click state locked once its pressed flag (override / key / pressTime) is not set
            if (!clickState.disabled) {
                Boolean pressed = clickState.down != null ? clickState.down
                        : Boolean.valueOf(clickState.key.pressed || clickState.holdTicks >= 1);
                if (!pressed) {
                    clickState.disabled = true;
                }
            }
        }
        if (this.field132) {
            clickState.disabled = true;
            this.field133 = true;
            this.field132 = false;
            return;
        }
        if (this.field133 && this.field131) {
            clickState.disabled = true;
            this.field133 = false;
            return;
        }
        if (this.isRotationWindowActive()) {
            ((com.adjustclient.module.util.MouseClickHelper) Adjust.mouseClickHelper).left.disabled = true;                // lock the attack key's click state
        }
    }

    @com.adjustclient.event.EventHandler
    public void onUserInputVerification(UserInputVerificationEvent e) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }
        if (InventoryStateTracker.inServerGui || InventoryStateTracker.inServerInventory) {
            return;
        }
        e.inputRequested = true;
    }

    @com.adjustclient.event.EventHandler
    public void onWorldReset(WorldResetEvent e) {
        if (this.isEnabled() && this.autoEnableTimer != null) {
            this.autoEnableTimer = null;
            this.setEnabled(false, true);
        }
    }

    @com.adjustclient.event.EventHandler(priority = -2)
    public void onSprint(com.adjustclient.event.SprintEligibilityEvent e) {
        if (!this.isEnabled() || this.placeReady == null || !this.placeReady) {
            return;
        }
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (!mode.canSprint()) {
            e.sprintEligible = false;
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.sprintingTicksLeft = 0;
        }
    }

    /** Pre-motion: tracks the base level (floor of the player's y) used by the tower / no-ascend logic. */
    @com.adjustclient.event.EventHandler
    public void onMotion(MotionEvent e) {
        if (!this.isEnabled() || !e.pre) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (player.onGround) {
            this.towerKeyOnGround = this.settings.isTowerKeyDown();
        }
        boolean onGround = player.onGround;
        if (player.isCollidedVertically && !onGround) {
            this.field130 = false;
            this.field116 = 0;
        }
        double posY = player.posY;
        int base = this.baseY;
        if (onGround || (double) base > posY || this.towering) {
            int y = (int) posY;
            if (this.field130) {
                if (base == y) {
                    this.field116 = 0;
                }
                this.field130 = false;
            } else if (base != y) {
                this.field116 = 0;
                this.baseY = y;
            }
        }
    }

    @com.adjustclient.event.EventHandler
    public void onTick(com.adjustclient.event.PreClientTickEvent e) {
        this.onTick();
    }

    public void onTick() {
        if (!this.isEnabled()) {
            return;
        }
        this.field137 = false;
        this.placement = null;
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.playerController.getCurrentGameType() == GameType.ADVENTURE
                || mc.playerController.getCurrentGameType() == GameType.SPECTATOR) {
            this.autoEnableTimer = null;
            this.setEnabled(false, true);
            return;
        }
        this.placementTracker.removeExpired();
        if (!GameStateUtil.isActive()) {
            if (this.autoEnableTimer != null) {
                this.autoEnableTimer = null;
                this.setEnabled(false, true);
            }
            return;
        }
        if (this.settings.isNoAscend() && this.autoEnableTimer == null) {
            if (!com.adjustclient.anticheat.feature.Features.SCAFFOLD_NO_ASCEND.isAllowed()) {
                if (this.autoEnableTimer != null) {
                    this.autoEnableTimer = null;
                    this.setEnabled(false, true);
                }
                return;
            }
        }
        this.noAscendActive = this.isNoAscendActive(true);
        // ground scan below the player with the planned rotation
        Integer ground = this.placementScanner.predict(player, new Vec2f(this.targetYaw, this.targetPitch));
        boolean nearGround = ground != null && ground.intValue() < 5;
        boolean hasBlock = this.selectBlock(nearGround);
        if (hasBlock) {
            this.validSlotTicks++;
            boolean ready;
            if (this.validSlotTicks < 2) {
                // the first tick after getting a block: not ready on Hypixel
                ready = !(ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                        && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL);
            } else {
                ready = true;
            }
            this.placeReady = ready ? Boolean.TRUE : Boolean.FALSE;
            this.field137 = true;
        } else {
            this.validSlotTicks = 0;
            this.placeReady = Boolean.FALSE;
        }

        if (this.placeReady) {
            this.rotating = true;
            this.safeWalk.update(ground);
            if (ground == null) {
                this.ticksSincePlace++;
            }
            if (this.canPlaceNow()) {
                // Safe walk + Sneak: sneak when nothing solid is below
                if (this.settings.isSafeWalk() && this.settings.isSafeWalkSneak()) {
                    BlockPos below = new BlockPos(player.posX, player.posY - 1.0D, player.posZ);
                    Block block = mc.theWorld.getBlockState(below).getBlock();
                    if (BlockUtil.canReplace(block, mc.theWorld, below)) {
                        this.safeWalk.startSneak();
                    }
                }
                if (ground != null) {
                    if (ground.intValue() <= this.settings.getExtend()) {        // "Extend"
                        this.placement = this.placementScanner.lastResult;
                        if (this.autoEnableTimer != null) {
                            if (this.autoEnableTimer.lastMs + 40L > System.currentTimeMillis()) {
                                this.placement = null;
                            }
                        }
                    }
                }
                if (this.placement != null) {
                    this.lastPlacePos = this.placement.pos;
                    this.lastPlaceBox = this.placement.placedPos;
                }
            }
        }
        boolean rotatedThisTick = false;
        if (this.isRotationWindowActive() && this.settings.isRotationsEnabled()) {
            ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
            boolean jumpPlanned = mode instanceof HypixelScaffoldMode && this.planHypixelJump(player);
            boolean airborne = player.onGround || this.willLandOnBlock(player.motionY) || jumpPlanned;
            if (player.onGround) {
                this.rotated = false;
            }
            boolean h = this.h(!airborne);
            if (h && airborne) {
                RotationModel jumpModel = InstantRotationModel.INSTANCE;
                if (mode instanceof HypixelScaffoldMode && this.placeDelay >= player.onGroundTicks) {
                    jumpModel = new ScaffoldLinearRotationModel(RandomUtil.nextFloat(55.133701F, 69.419998F));
                }
                java.util.Objects.requireNonNull(com.adjustclient.module.impl.movement.sprint.SprintModule.INSTANCE.rotateMode);
                rotateTowardsMovement(jumpModel, this.targetPitch, 0.3F);
                this.field132 = true;
                this.field131 = true;
            } else {
                if (!h) {
                    this.field131 = false;
                }
                boolean snapGuard = this.S();
                if (mode instanceof HypixelScaffoldMode) {
                    this.k(false);
                    rotatedThisTick = true;
                } else if (!snapGuard || this.placement != null) {
                    this.k(snapGuard);
                    rotatedThisTick = true;
                }
            }
            this.field126 = false;
        }
        ScaffoldMode currentMode = (ScaffoldMode) this.settings.mode.getValue();
        if (currentMode instanceof HypixelScaffoldMode && this.placeDelay == 0) {
            this.placeDelay = 1;
        }
        if (this.placeReady != null && this.placeReady && !rotatedThisTick) {
            this.placement = this.placementFinder.recompute(this.placement, null);
        }
        // timed enable (auto enable): switch off again once the player stands still on a block
        if (this.autoEnableTimer != null) {
            if (player.onGround) {
                long now = System.currentTimeMillis();
                if (this.autoEnableTimer.lastMs + 250L <= now) {
                    if (ground != null && ground.intValue() < 3) {
                        return;
                    }
                    Timer velocity = ServerTracker.INSTANCE.field116;
                    if (velocity != null) {
                        if (velocity.lastMs + 650L <= System.currentTimeMillis()) {
                            this.autoEnableTimer = null;
                            this.setEnabled(false, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Hypixel only: decides whether a jump should be planned this tick (the player is high enough in the air, the
     * h() conditions hold and there is no placement delay): the jump is simulated and the result compared with the real
     * position; the next placement delay is drawn from [1, 4).
     */
    private boolean planHypixelJump(EntityPlayerSP player) {
        if (player.onGround) {
            return false;
        }
        int threshold = mc.gameSettings.keyBindJump.pressed ? 6 : 9;
        if (threshold >= player.offGroundTicks) {
            return false;
        }
        if (!this.h(false)) {
            return false;
        }
        if (this.placeDelay >= 1) {
            return false;
        }
        com.adjustclient.util.SimulationPlayer sim = new Simulation(player).player;
        sim.tick();
        if (!sim.onGround) {
            if ((int) player.posY <= (int) sim.posY) {
                return false;
            }
        }
        int delay = RandomUtil.nextInt(RandomUtil.RANDOM, 1, 4);
        this.placeDelay = delay;
        if (this.placement != null) {
            if (delay < 2) {
                this.placeDelay = 2;
            }
            return false;
        }
        return true;
    }

    @com.adjustclient.event.EventHandler
    public void onMoveInput(com.adjustclient.event.MovementInputEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (this.rotating
                && Math.abs(getUsernameHash()) % 3 + 20 > this.ticksSincePlace
                && this.noAscendActive
                && !this.placementTracker.hasPending()
                && !player.capabilities.isFlying
                && this.settings.isAutoJump()) {                                               // "Auto jump"
            ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
            if (mode instanceof HypixelScaffoldMode) {
                if (ServerTracker.INSTANCE.ticks < 1) {
                    player.jumpTicks = 0;
                    e.jump = true;
                }
            } else if (player.onGround && this.placeDelay < player.onGroundTicks) {
                player.jumpTicks = 0;
                e.jump = true;
            }
        }
        // sneak countdown ("Un-sneak delay" of the module)
        int sneak = this.sneakTicks;
        if (sneak >= 1) {
            e.sneak = true;
            this.sneakTicks = sneak - 1;
            if (sneak == 1) {
                if (!mc.thePlayer.onGround) {
                    this.sneakTicks = 1;
                }
            }
        }
        if (e.jump && this.placeDelay != 0) {
            if (this.placeDelay >= player.onGroundTicks) {
                // simulate the jump: if the simulated player is on the ground afterwards the jump is delayed
                Simulation sim = new Simulation();
                sim.simulateTick(player, true);
                sim.player.movementInput.jump = false;
                sim.player.tick();
                if (sim.player.onGround) {
                    e.jump = false;
                    player.jumpTicks = Math.max(player.jumpTicks, 1);
                }
            }
        }
        if (!e.jump) {
            return;
        }
        if (mc.thePlayer.jumpTicks < 1) {
            this.placeDelay = 0;
        }
    }

    /** Render > "Block placements": draws the block that appears (orange) and the clicked block (green). */
    @com.adjustclient.event.EventHandler
    public void onRender3D(Render3DEvent e) {
        if (!this.isEnabled() || !this.settings.isBlockPlacements()) {
            return;
        }
        if (this.lastPlaceBox == null || this.lastPlacePos == null) {
            return;
        }
        RenderUtil.begin3d();
        RenderManager rm = mc.getRenderManager();
        double viewX = rm.viewerPosX, viewY = rm.viewerPosY, viewZ = rm.viewerPosZ;
        AxisAlignedBB placed = unitBox(new Vec3(this.lastPlaceBox.getX() - viewX, this.lastPlaceBox.getY() - viewY,
                this.lastPlaceBox.getZ() - viewZ));
        new Color(200, 120, 32, 60);
        RenderUtil.glColor(0x3CC87820);
        RenderUtil.drawFilledBox(placed);
        RenderUtil.drawOutlinedBox(placed);
        AxisAlignedBB clicked = unitBox(new Vec3(this.lastPlacePos.getX() - viewX, this.lastPlacePos.getY() - viewY,
                this.lastPlacePos.getZ() - viewZ));
        new Color(10, 220, 32, 60);
        RenderUtil.glColor(0x3C0ADC20);
        RenderUtil.drawFilledBox(clicked);
        RenderUtil.drawOutlinedBox(clicked);
        RenderUtil.end3d();
    }

    private static AxisAlignedBB unitBox(Vec3 min) {
        return new AxisAlignedBB(min.xCoord, min.yCoord, min.zCoord, min.xCoord + 1.0D, min.yCoord + 1.0D, min.zCoord + 1.0D);
    }

    /**
     * Executes a placement (unless "Wait for click" is on and the click limiter does not allow a click yet), reports the
     * Scaffold technique that was used to the server-side feature log (JUMP / NO_ASCEND / SNEAK / FLAT), remembers the
     * placement and notifies the placement tracker, the mode and the block counter.
     */
    private void handlePlacement(PlacementTarget placement) {
        if (this.settings.isWaitForClick()) {
            // Wait for a fake click on the existing block: only when the CPS limiter has a click ready
            if (!Adjust.cpsCounter.canClick(this.settings.cps)) {
                return;
            }
        }
        this.executePlacement(placement);
        this.lastPlacement = placement;
        this.placementTracker.recordPlacementTime(placement.pos);
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        mode.onPlace(placement);
        this.placementTracker.trackPlacement(placement);
        this.settings.blockCountHud.onPlace(placement);
    }

    @SuppressWarnings("unused")
    private void vendorReportPlacementBypass(PlacementTarget placement, boolean serverSneakState) {
        com.adjustclient.service.telemetry.BypassFeature feature;
        if (placement.pos.offset(placement.facing).getY() + 1 > this.baseY) {
            feature = com.adjustclient.service.telemetry.BypassFeature.SCAFFOLD_JUMP;
        } else if (this.settings.isNoAscend()) {
            feature = com.adjustclient.service.telemetry.BypassFeature.SCAFFOLD_NO_ASCEND;
        } else if ((this.settings.isSafeWalk() && this.settings.isSafeWalkSneak())
                || (mc.thePlayer.movementInput != null && mc.thePlayer.movementInput.sneak && !serverSneakState)) {
            feature = com.adjustclient.service.telemetry.BypassFeature.SCAFFOLD_SNEAK;
        } else {
            feature = com.adjustclient.service.telemetry.BypassFeature.SCAFFOLD_FLAT;
        }
        ServiceUtil.reportFeatureUse(feature);
    }

    public int simulateRotationTicks(Vec2f target) {
        boolean snapGuard = this.S();
        EntityPlayerSP player = mc.thePlayer;
        RotationModel model = this.getRotationModel(snapGuard, player.onGround || !this.rotated, target.x);
        if (!(model instanceof ScaffoldLinearRotationModel || model instanceof InstantRotationModel
                || model instanceof LinearRotationModel)) {
            return 0;
        }
        // --- simulation
        Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        float tolerance = Math.max(Math.ulp(target.x), Math.ulp(target.y));
        int ticks = 0;
        while (true) {
            Vec2f desired = applyModel(InstantRotationModel.INSTANCE, current, target, 1.0F);
            Vec2f next = applyModel(model, current, target, 1.0F);
            Vec2f[] mouseRotations = {desired, next};
            float sensitivity = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float mouseFactor = sensitivity * sensitivity * sensitivity * 8.0F;
            for (int index = 0; index < mouseRotations.length; index++) {
                Vec2f rotation = mouseRotations[index];
                float yawDelta = MathHelper.wrapAngleTo180_float(rotation.x - current.x);
                float yawInput = ((yawDelta + current.x) - current.x) / 0.15F / mouseFactor;
                float pitchInput = ((rotation.y - current.y + current.y) - current.y) / 0.15F / mouseFactor;
                float yaw = current.x + (yawInput * mouseFactor) * 0.15F;
                float pitch = current.y + (pitchInput * mouseFactor) * 0.15F;
                if (!RotationHandler.INSTANCE.field19) {
                    pitch = MathHelper.clamp_float(pitch, -90.0F, 90.0F);
                }
                mouseRotations[index] = new Vec2f(yaw, pitch);
            }
            desired = mouseRotations[0];
            next = mouseRotations[1];
            float yawError = MathHelper.wrapAngleTo180_float(next.x - desired.x);
            float pitchError = next.y - desired.y;
            ticks++;
            if (tolerance >= Math.abs(yawError) && tolerance >= Math.abs(pitchError)) {
                return ticks - 1;
            }
            if (ticks >= 4) {
                return 4;
            }
            current = next;
        }
    }

    boolean isAboveBuildLimit(Entity entity) {
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        int y = (int) entity.posY - 1;
        return mode.isAboveBuildLimit(y);
    }

    /** Module.canToggle(boolean): Scaffold cannot be toggled while the player is null, in Adventure or Spectator mode. */
    @Override
    public boolean canToggle(boolean enabling) {
        if (enabling || this.autoEnableTimer == null) {
            if (mc.thePlayer == null) {
                return false;
            }
            if (!enabling) {
                return true;
            }
            GameType type = mc.playerController.getCurrentGameType();
            return type != GameType.SPECTATOR && type != GameType.ADVENTURE;
        }
        this.autoEnableTimer = null;
        return false;
    }

    boolean isUsableBlock(int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) {
            return false;
        }
        if (ServerTracker.INSTANCE.hotbarBlockTracker.isPredictedEmpty(slot, stack)) {
            return false;
        }
        Block block = ((ItemBlock) stack.getItem()).getBlock();
        // game specific exclusion: in one particular Hypixel game one particular block is not allowed
        if (Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS /* HypixelGame */ && block == Blocks.pumpkin /* Block */) {
            return false;
        }
        if (BlockItemType.BLACKLIST.contains(block)) {
            return false;
        }
        if (BlockUtil.INTERACTABLE_BLOCKS != null && BlockUtil.INTERACTABLE_BLOCKS.contains(block)) {
            return false;
        }
        if (block instanceof BlockDoor || block instanceof BlockFence || block instanceof BlockFenceGate
                || block instanceof BlockStairs || block instanceof BlockWall || block instanceof BlockPumpkin
                || block instanceof net.minecraft.block.BlockBasePressurePlate || block instanceof net.minecraft.block.BlockBush) {
            return false;
        }
        if (block == Blocks.tnt /* Block */) {
            // this one block is refused on one server type and in two Hypixel games
            com.adjustclient.util.anticheat.blocker.BlockerState server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null && server.antiCheatModel.serverType == ServerType.MINEMEN_CLUB /* ServerType */) {
                return false;
            }
            com.adjustclient.hypixel.HypixelGameState game = Adjust.gameStateTracker.state;
            if (game.startTimer != null && (game.game == HypixelGame.BED_WARS || game.game == HypixelGame.BED_WARS_PRACTICE)) {
                return false;
            }
        }
        return block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() == 1.0D
                && block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() == 1.0D
                && block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() == 1.0D;
    }

    /**
     * Chooses the rotation model: a fast model while a timed enable is running / in "fast" situations, otherwise the
     * model of the "Rotations" option; a timed enable draws a randomised speed. {@code instant} forces the Instant model.
     */
    private RotationModel getRotationModel(boolean instant, boolean firstTurn, float yaw) {
        EntityPlayerSP player = mc.thePlayer;
        RotationModel model;
        boolean fast = this.autoEnableTimer != null || this.field131;
        if (!fast) {
            ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
            fast = mode instanceof HypixelScaffoldMode && ServiceUtil.isActiveModel(ServerType.HYPIXEL)
                    && MovementFixModule.INSTANCE.isEnabled();
        }
        if (fast) {
            float limit = firstTurn ? 73.0F : 43.0F;
            if (!firstTurn && Math.abs(MathHelper.wrapAngleTo180_float(player.rotationYaw - yaw)) >= 1.0F) {
                limit -= this.rotationJitter;
            }
            model = new ScaffoldLinearRotationModel(limit - RandomUtil.nextFloat(0.1F, (float) ServiceUtil.movementAdjustment + 1.0F));
        } else {
            RotationSetting rotation = this.settings.rotation;
            model = rotation.mode.getValue().factory.apply(rotation);
        }
        if (this.autoEnableTimer != null) {
            model = this.autoEnableModel(player, yaw);
        }
        return instant ? InstantRotationModel.INSTANCE : model;
    }

    /** Randomised model of a timed enable: slower when the target is behind the player, Instant when moving fast. */
    private RotationModel autoEnableModel(EntityPlayerSP player, float yaw) {
        Random random = RandomUtil.RANDOM;
        float speed = 20.0F - (random.nextFloat() * 0.9F + 0.1F);
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(player.rotationYaw - yaw));
        if (diff < 150.0F) {
            if (diff > 35.0F) {
                speed += 30.0F;
            }
        } else {
            speed += 110.0F;
        }
        if (Math.hypot(player.motionX, player.motionZ) >= 0.7D) {
            random.nextFloat();
            return InstantRotationModel.INSTANCE;
        }
        return new ScaffoldLinearRotationModel((double) speed);
    }

    private static void rotateTowardsMovement(RotationModel model, float pitch, float yawSpread) {
        float yaw = com.adjustclient.rotation.handler.RenderRotationTracker.getRotation().x;
        boolean back = mc.gameSettings.keyBindBack.pressed;
        if (mc.gameSettings.keyBindForward.pressed || back) {
            if (back) {
                yaw += 180.0F;
            }
        } else {
            if (mc.gameSettings.keyBindLeft.pressed) {
                yaw -= 90.0F;
            }
            if (mc.gameSettings.keyBindRight.pressed) {
                yaw += 90.0F;
            }
        }
        yaw = RandomUtil.jitter(yaw, yawSpread);
        float randomPitch = pitch + (RandomUtil.RANDOM.nextFloat() - 0.5F) * 0.03F;
        RotationHandler.rotate(model, yaw, randomPitch);
    }

    /**
     * Applies {@code model} to the rotation step from {@code from} to {@code to} and snaps the result to the mouse
     * sensitivity grid ({@code (0.6 * sensitivity + 0.2)^3 * 8 * 0.15}), so the rotation looks like real mouse input.
     * The Instant model is blended directly with {@code f}.
     */
    static Vec2f applyModel(RotationModel model, Vec2f from, Vec2f to, float f) {
        Vec2f stepped;
        if (model instanceof InstantRotationModel) {
            float yawDiff = MathHelper.wrapAngleTo180_float(to.x - from.x);
            stepped = new Vec2f(yawDiff * f + from.x, (to.y - from.y) * f + from.y);
        } else {
            stepped = model.apply(from, to, f);
        }
        float sensitivity = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        double gcd = (double) sensitivity * sensitivity * sensitivity * 8.0D * (double) 0.15F;
        float yaw = (float) ((int) Math.round((stepped.x - from.x) / gcd) * gcd + from.x);
        float pitch = (float) ((int) Math.round((stepped.y - from.y) / gcd) * gcd + from.y);
        return new Vec2f(yaw, pitch);
    }

    /**
     * Executes the placement: makes the game believe the crosshair points at the block (objectMouseOver), resets the
     * right-click delay, arms a right click in the click state and records the click for the CPS limiter.
     */
    private void executePlacement(PlacementTarget placement) {
        Vec3 hit = placement.hitVec;
        mc.rightClickDelayTimer = 0;
        mc.objectMouseOver = new MovingObjectPosition(new Vec3(hit.xCoord, hit.yCoord, hit.zCoord), placement.facing,
                placement.pos);
        ServerTracker.INSTANCE.mouseOverOverridden = true;
        com.adjustclient.module.util.SwingMouseClick click = ((com.adjustclient.module.util.MouseClickHelper) Adjust.mouseClickHelper).right;
        click.clicks = Integer.valueOf(1); // right mouse button
        click.down = Boolean.TRUE;
        click.holdTicks = 0;
        if (!this.settings.isClientsideSwing()) {                      // Addons > Clientside > Swing
            click.swing = false;
        }
        Adjust.cpsCounter.recordClick();
    }

    private boolean h(boolean flag) {
        if (!MovementFixModule.INSTANCE.isEnabled()) {
            return false;
        }
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        EntityPlayerSP player = mc.thePlayer;
        if (mode instanceof HypixelScaffoldMode) {
            if (!this.settings.isAscendOnJump() && mc.gameSettings.keyBindJump.pressed) {      // "Ascend on jump"
                return false;
            }
            if (this.settings.isSnap()) {
                double direction = Math.abs(MovementUtil.getDirection() % 90.0D);
                if (direction > 5.0D && direction < 85.0D) {
                    return false;
                }
            }
        }
        if (!this.settings.isAllowSprint()) {
            return false;
        }
        if (flag) {
            if (player.movementInput != null && player.movementInput.sneak && !player.sleeping) {
                return false;
            }
        }
        // no solid block directly above the player's box
        AxisAlignedBB above = player.getEntityBoundingBox().offset(0.0D, 1.0D, 0.0D);
        if (BlockUtil.isColliding(above)) {
            return false;
        }
        if (!mc.gameSettings.keyBindJump.pressed) {
            if (!this.isNoAscendActive(false)) {
                return false;
            }
            if (!this.settings.isAutoJump()) {                                            // "Auto jump"
                return false;
            }
        }
        if (!MovementUtil.isMoving()) {
            return false;
        }
        if (!mc.gameSettings.keyBindSprint.pressed) {
            return false;
        }
        if (ServerTracker.INSTANCE.ticks >= 1) {
            return false;
        }
        if (this.autoEnableTimer != null) {
            return false;
        }
        return this.settings.isNoAscend();
    }

    /** HUD suffix of the module: the name of the selected mode. */
    @Override
    public String getSuffix() {
        return ((ScaffoldMode) this.settings.mode.getValue()).getName();
    }

    private boolean selectBlock(boolean force) {
        InventoryPlayer inventory = mc.thePlayer.inventory;
        boolean usable = false;
        int held = inventory.m();
        if (held < 9 && held >= 0) {
            ItemStack stack = inventory.mainInventory[inventory.m()];
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                usable = this.isUsableBlock(inventory.m(), stack);
                if (usable && !Adjust.slotSpoofer.active) {         // slot spoofer not active
                    return true;
                }
            }
        }
        if (!this.settings.isChangeSlot()) {                       // Addons > "Change slot"
            return usable;
        }
        Integer best = WorldUtil.findHotbarSlot(inventory.m(), slotIndex -> {
            ItemStack candidate = mc.thePlayer.inventory.mainInventory[slotIndex];
            if (candidate != null && candidate.getItem() instanceof ItemBlock) {
                return this.isUsableBlock(slotIndex, candidate);
            }
            return false;
        });
        if (best == null) {
            return usable;
        }
        if (!force && !this.slotChanged) {
            return usable;
        }
        boolean clientsideSwap = this.settings.isClientsideSwap();
        com.adjustclient.game.player.SlotSpoofer spoofer = Adjust.slotSpoofer;
        spoofer.slot = best.intValue();
        spoofer.active = true;
        spoofer.field8 = spoofer.field12;
        spoofer.field17 = false;
        spoofer.renderSpoofed = clientsideSwap;
        this.slotChanged = true;
        if (this.candidateSlot != null && this.candidateSlot.intValue() != best.intValue()) {
            if (this.settings.mode.getValue() instanceof HypixelScaffoldMode) {
                this.sneakTicks = 2;
            }
        }
        if (this.candidateSlot == null || this.candidateSlot.intValue() != best.intValue()) {
            this.candidateSlot = best;
        }
        return true;
    }

    /**
     * No ascend is currently in effect: the addon is on and supported by the mode, no placement is pending, the module
     * is not in a timed enable, the player is not towering, and the player may not just walk up.
     */
    private boolean isNoAscendActive(boolean flag) {
        if (this.autoEnableTimer != null) {
            return false;
        }
        if (!this.settings.isNoAscend()) {
            return false;
        }
        if (this.placementTracker != null && this.placementTracker.hasPending()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (mode instanceof HypixelScaffoldMode && player.isPotionActive(Potion.moveSpeed /* Potion */)) {
            return false;
        }
        boolean force = false;
        if (flag) {
            force = this.noAscendActive && !player.onGround;
        }
        if (!this.settings.isNoAscendActive(force)) {
            return false;
        }
        if (this.towering) {
            return false;
        }
        if (this.settings.isTower() && this.towerKeyOnGround) {
            return false;
        }
        if (this.isAboveBuildLimit(player)) {
            return false;
        }
        if (!MovementUtil.isMoving() && !force) {             // retain only an already-active airborne state
            return false;
        }
        return !player.isPotionActive(Potion.jump);
    }

    /**
     * Applies the rotation for this tick: chooses the model ({@link #getRotationModel}), updates the rotation jitter and
     * the {@link #rotated} flag, then either rotates through the plain model (Linear / Instant / Scaffold-linear models
     * go through the {@link PlacementFinder} with {@link #applyModel} as rotation function) or wraps the model in a
     * {@link ScaffoldRotationModel} and hands it to the rotation helper.
     */
    private void k(boolean instant) {
        RotationModel model = this.getRotationModel(instant, !this.rotated, this.targetYaw);
        EntityPlayerSP player = mc.thePlayer;
        final Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        if (this.T()) {
            if (this.rotated) {
                float diff = MathHelper.wrapAngleTo180_float(player.rotationYaw - this.targetYaw);
                if (Math.abs(diff) >= 1.0F) {
                    float jitter = this.rotationJitter;
                    if (jitter < 10.0F) {
                        this.rotationJitter = jitter + RandomUtil.nextFloat(3.0F, 4.0F);
                    }
                } else {
                    this.rotationJitter = 0.0F;
                }
            } else {
                this.field131 = false;
                this.rotationJitter = 0.0F;
                this.rotated = true;
            }
        } else {
            this.rotated = false;
        }
        boolean simple = model instanceof ScaffoldLinearRotationModel || model instanceof InstantRotationModel
                || model instanceof LinearRotationModel;
        if (!simple) {
            // other models: let the rotation helper run the (wrapped) model over several ticks
            RotationHandler.INSTANCE.rotate(new ScaffoldRotationModel(model), this.targetYaw, this.targetPitch);
            this.placement = this.placementFinder.recompute(this.placement, null);
            return;
        }
        if (this.placementFinder != null) {
            UnaryOperator<Vec2f> rotationFn = rot -> applyModel(model, current, rot, 1.0F);
            this.placement = this.placementFinder.recompute(this.placement, rotationFn);
            if (this.placement == null) {
                RotationHandler.INSTANCE.rotate(new ScaffoldRotationModel(model), this.targetYaw, this.targetPitch);
                return;
            }
            if (this.placementFinder.useRaycast()) {
                this.targetYaw = this.placement.rotation.x;
                this.targetPitch = this.placement.rotation.y;
                RotationHandler.INSTANCE.rotate(InstantRotationModel.INSTANCE, this.targetYaw, this.targetPitch);
            } else {
                RotationHandler.INSTANCE.rotate(new ScaffoldRotationModel(model), this.targetYaw, this.targetPitch);
            }
        }
    }

    /**
     * Enable: derives the initial target rotation from the movement keys (looking backwards, 180 degrees +- 0.5 away
     * from the movement direction) and creates the helper objects (placement finder with its solver, placement tracker,
     * ground scanner, safe-walk helper); resets all state flags.
     */
    @Override
    public void onEnable() {
        this.field124 = false;
        Vec2f preset = ServerTracker.INSTANCE.rotation;
        if (preset != null) {
            EntityPlayerSP player = mc.thePlayer;
            float yaw = player.rotationYaw;
            GameSettings gs = mc.gameSettings;
            float forward = (gs.keyBindForward.pressed ? 1.0F : 0.0F) - (gs.keyBindBack.pressed ? 1.0F : 0.0F);
            float strafe = (gs.keyBindLeft.pressed ? 1.0F : 0.0F) - (gs.keyBindRight.pressed ? 1.0F : 0.0F);
            float angle;
            if (forward < 0.0F) {
                yaw += 180.0F;
                angle = -45.0F;
            } else if (forward > 0.0F) {
                angle = 45.0F;
            } else {
                angle = 90.0F;
            }
            if (strafe > 0.0F) {
                yaw -= angle;
            }
            if (strafe < 0.0F) {
                yaw += angle;
            }
            this.targetYaw = yaw - (RandomUtil.RANDOM.nextFloat() - 0.5F + 180.0F);
            this.targetPitch = preset.y;
        }
        this.rotating = false;
        this.placementFinder = new PlacementFinder(this);
        this.placementTracker = new PlacementTracker(this);
        this.placementScanner = new PlacementPredictor(this);
        if (this.safeWalk != null) {
            EventBus.unregister(this.safeWalk);
        }
        this.safeWalk = new SafeWalkHelper(this);
        EventBus.register(this.safeWalk);
        EntityPlayerSP player = mc.thePlayer;
        this.baseY = player == null ? Integer.MAX_VALUE : (int) player.posY;
        this.slotChanged = false;
        this.field133 = false;
        this.field132 = false;
        this.rotated = true;
        this.rotationJitter = 0.0F;
        this.field137 = false;
        this.towering = false;
        this.towerKeyOnGround = false;
    }

    /** Takes over the movement input (an "input override" is registered at the input manager). */
    public void holdInput() {
        if (this.inputOverride != null) {
            return;
        }
        this.inputOverride = Adjust.inboundPacketQueue.addHold(null, false);
    }

    @com.adjustclient.event.EventHandler
    public void onPacket(com.adjustclient.event.DirectionalPacketEvent e) {
        if (e.isCancelled() || e.outgoing) {
            return;
        }
        Packet<?> packet = e.packet;
        if (packet == null) {
            return;
        }
        if (packet instanceof S01PacketJoinGame) {
            if (this.isEnabled()) {
                if (this.autoEnableTimer != null) {
                    this.autoEnableTimer = null;
                    return;
                }
                if (mc.thePlayer != null) {
                    this.flags &= ~2;
                    this.onDisable();
                    if (this.notifyToggle) {
                        com.adjustclient.vfs.VirtualFileSystem files = Adjust.INSTANCE.fileSystem;
                        if (!files.loading && ConfigurationService.configurationsAvailable && ConfigurationService.configurationReceived && ServiceUtil.pendingTasksReady) {
                            files.markDirty();
                        }
                    }
                }
            }
        } else if (packet instanceof S23PacketBlockChange && this.isEnabled()) {
            if (Thread.currentThread() == mc.mcThread) {
                if (this.placementTracker != null) {
                    this.placementTracker.onBlockChange((S23PacketBlockChange) packet);
                }
            } else {
                final S23PacketBlockChange blockChange = (S23PacketBlockChange) packet;
                mc.addScheduledTask(() -> {
                    if (this.placementTracker != null) {
                        this.placementTracker.onBlockChange(blockChange);
                    }
                });
            }
        }
    }

    /**
     * Enables Scaffold temporarily (called by other modules): remembers the time so the module switches itself off
     * again (see {@link #onTick()}).
     */
    public void enableTemporarily() {
        if (this.isEnabled()) {
            return;
        }
        this.autoEnableTimer = new Timer();
        this.setEnabled(true, true);
    }

    /** Gives the input back: clears the click queue and removes the input override on the input manager. */
    public void releaseInput() {
        this.field136 = false;
        this.clickQueue.flush();
        PacketHold override = this.inputOverride;
        if (override != null) {
            InboundPacketQueue manager = Adjust.inboundPacketQueue;               // input manager
            manager.processNow(() -> manager.removeHold(override));
            this.inputOverride = null;
        }
    }

    /** Mode setting changed (ScaffoldSettings "Mode" listener): gives the input back and stops towering. */
    void onModeChange() {
        this.releaseInput();
        this.towering = false;
    }

    /** Disable: resets every field and gives the sneak key and the input back to the player. */
    @Override
    public void onDisable() {
        this.field131 = false;
        this.placement = null;
        this.lastPlacement = null;
        this.lastPlacePos = null;
        this.lastPlaceBox = null;
        ScaffoldMode mode = (ScaffoldMode) this.settings.mode.getValue();
        if (mode instanceof NormalScaffoldMode) {
            ((NormalScaffoldMode) mode).towering = false;
        }
        this.field130 = false;
        this.placementScanner = null;
        this.validSlotTicks = 0;
        this.ticksSincePlace = 100;
        this.placeReady = null;
        this.placementTracker = null;
        this.noAscendActive = false;
        // give the sneak key back: pressed = whether the physical key is down
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        boolean down = GameSettings.isKeyDown(sneak);      // Keyboard.isKeyDown / Mouse.isButtonDown for the bound key
        if (!down) {
            sneak.pressTime = 0;
        }
        sneak.pressed = down;
        if (this.safeWalk != null) {
            EventBus.unregister(this.safeWalk);
            if (this.safeWalk.sneaking) {
                this.safeWalk.sneaking = false;
                this.safeWalk.unsneakTicks = 0;
            }
        }
        this.placementFinder = null;
        this.autoEnableTimer = null;
        this.towering = false;
        this.towerKeyOnGround = false;
        this.field126 = false;
        this.field116 = 0;
        this.candidateSlot = null;
        this.sneakTicks = 0;
        this.placeDelay = 0;
        this.field136 = false;
        this.clickQueue.flush();
        if (this.inputOverride != null) {
            InboundPacketQueue manager = Adjust.inboundPacketQueue;
            PacketHold override = this.inputOverride;
            manager.processNow(() -> manager.removeHold(override));
        }
        this.inputOverride = null;
    }
}
