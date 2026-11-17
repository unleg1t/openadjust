package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.Adjust;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventBus;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.JumpEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MovementInputEvent;
import com.adjustclient.event.PreClientTickEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.event.UserInputVerificationEvent;
import com.adjustclient.event.WorldResetEvent;
import com.adjustclient.game.packet.InboundPacketQueue;
import com.adjustclient.game.packet.PacketHold;
import com.adjustclient.game.player.InventoryStateTracker;
import com.adjustclient.game.player.SlotSpoofer;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.HypixelGameState;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.module.impl.movement.MovementFixModule;
import com.adjustclient.module.impl.movement.inventorymove.InventoryMoveModule;
import com.adjustclient.module.impl.movement.sprint.SprintModule;
import com.adjustclient.module.impl.player.inventory.item.type.BlockItemType;
import com.adjustclient.module.util.CpsCounter;
import com.adjustclient.module.util.CpsSettings;
import com.adjustclient.module.util.MouseClickHelper;
import com.adjustclient.module.util.SwingMouseClick;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.LinearRotationModel;
import com.adjustclient.rotation.RotationModel;
import com.adjustclient.rotation.RotationSetting;
import com.adjustclient.rotation.handler.RenderRotationTracker;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.server.ServerType;
import com.adjustclient.service.config.ConfigurationService;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.GameStateUtil;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.PacketQueue;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.BlockerState;
import com.adjustclient.vfs.VirtualFileSystem;
import java.awt.Color;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoor;
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
import net.minecraft.init.Blocks;
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

public final class ScaffoldModule extends Module {

    public static ScaffoldModule INSTANCE;

    public ScaffoldSettings settings;
    public PlacementFinder placementFinder;
    private PlacementTracker placementTracker;
    private SafeWalkHelper safeWalk;
    private PlacementPredictor placementScanner;
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
    public int baseY;
    public boolean placeReady;
    private boolean rotating;
    private boolean firstTurn;
    private boolean slotChanged;
    public boolean noAscendActive;
    boolean field130;
    private boolean field131;
    private boolean field132;
    private boolean field133;
    private boolean towerKeyOnGround;
    private boolean field136;
    public boolean field137;

    public ScaffoldModule() {
        super("Scaffold", Category.MOVEMENT);
        INSTANCE = this;
        this.settings = new ScaffoldSettings(this);
        this.placementFinder = new PlacementFinder(this);
        this.placementTracker = new PlacementTracker(this);
        this.safeWalk = new SafeWalkHelper(this);
        this.placementScanner = new PlacementPredictor(this);
        this.clickQueue = new PacketQueue();
        EventBus.register(this.safeWalk);
        this.notifyToggle = false;
    }

    private void resetState() {
        this.placeReady = false;
        this.ticksSincePlace = 100;
        this.placement = null;
        this.lastPlacement = null;
        this.lastPlacePos = null;
        this.lastPlaceBox = null;
        this.rotating = false;
        this.firstTurn = false;
        this.rotationJitter = 0.0F;
        this.slotChanged = false;
        this.validSlotTicks = 0;
        this.candidateSlot = null;
        this.noAscendActive = false;
        this.field130 = false;
        this.field131 = false;
        this.field132 = false;
        this.field133 = false;
        this.towerKeyOnGround = false;
        this.field137 = false;
        this.placementFinder.partner.yawOffset = null;
        this.placementTracker.clear();
        this.placementScanner.lastResult = null;
    }

    private boolean canPlaceNow() {
        ScaffoldMode mode = this.settings.mode.getValue();
        if (!mode.canPlaceInGame()) {
            return false;
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
        SwingMouseClick attackState = ((MouseClickHelper) Adjust.mouseClickHelper).left;
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
            ScaffoldMode mode = this.settings.mode.getValue();
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
        SwingMouseClick click = ((MouseClickHelper) Adjust.mouseClickHelper).right;
        click.clicks = Integer.valueOf(1);
        click.down = Boolean.TRUE;
        click.holdTicks = 0;
        click.swing = false;
        Adjust.cpsCounter.recordClick();
    }

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

    private boolean S() {
        if (!this.settings.isSnap()) {
            return false;
        }
        return this.settings.mode.getValue().canSnap();
    }

    private boolean T() {
        if (this.autoEnableTimer != null) {
            return true;
        }
        if (this.field131) {
            return true;
        }
        return this.settings.mode.getValue().useFastRotations();
    }

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

    @EventHandler(priority = 62)
    public void onPreUpdate(IngameActionEvent e) {
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
        SwingMouseClick clickState = ((MouseClickHelper) Adjust.mouseClickHelper).right;
        if (this.placeReady) {
            if (this.placement != null) {
                this.lastPlacePos = this.placement.pos;
                this.lastPlaceBox = this.placement.placedPos;
                this.handlePlacement(this.placement);
            } else {
                this.prepareClick();
            }
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
            ((MouseClickHelper) Adjust.mouseClickHelper).left.disabled = true;
        }
    }

    @EventHandler
    public void onUserInputVerification(UserInputVerificationEvent e) {
        if (!this.isEnabled() || mc.thePlayer == null) {
            return;
        }
        if (InventoryStateTracker.isInServersideGui()) {
            return;
        }
        e.inputRequested = true;
    }

    @EventHandler
    public void onWorldReset(WorldResetEvent e) {
        if (this.isEnabled() && this.autoEnableTimer != null) {
            this.autoEnableTimer = null;
            this.setEnabled(false, true);
        }
    }

    @EventHandler(priority = -2)
    public void onSprint(SprintEligibilityEvent e) {
        if (!this.isEnabled() || !this.placeReady) {
            return;
        }
        ScaffoldMode mode = this.settings.mode.getValue();
        if (!mode.canSprint()) {
            e.sprintEligible = false;
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.sprintingTicksLeft = 0;
        }
    }

    @EventHandler
    public void onJump(JumpEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        if (this.noAscendActive && mc.thePlayer.onGround
                && this.settings.isTower() && this.settings.isTowerKeyDown()) {
            this.noAscendActive = false;
        }
    }

    @EventHandler
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
        }
        double posY = player.posY;
        ScaffoldMode mode = this.settings.mode.getValue();
        if (onGround || (double) this.baseY > posY || mode.towering) {
            if (this.field130) {
                this.field130 = false;
            } else {
                this.baseY = (int) posY;
            }
        }
    }

    @EventHandler
    public void onTick(PreClientTickEvent e) {
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
            if (!Features.SCAFFOLD_NO_ASCEND.isAllowed()) {
                if (this.autoEnableTimer != null) {
                    this.autoEnableTimer = null;
                    this.setEnabled(false, true);
                }
                return;
            }
        }
        this.noAscendActive = this.isNoAscendActive(true);
        Integer ground = this.placementScanner.predict(player, new Vec2f(this.targetYaw, this.targetPitch));
        boolean nearGround = ground != null && ground.intValue() < 5;
        boolean hasBlock = this.selectBlock(nearGround);
        if (hasBlock) {
            this.validSlotTicks++;
            boolean ready;
            if (this.validSlotTicks < 2) {
                ready = !(ServerTracker.INSTANCE.serverState != null && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                        && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL);
            } else {
                ready = true;
            }
            this.placeReady = ready;
            this.field137 = true;
        } else {
            this.validSlotTicks = 0;
            this.placeReady = false;
        }

        if (this.placeReady) {
            this.rotating = true;
            this.safeWalk.update(ground);
            if (ground == null) {
                this.ticksSincePlace++;
            }
            if (this.canPlaceNow()) {
                if (this.settings.isSafeWalk() && this.settings.isSafeWalkSneak()) {
                    BlockPos below = new BlockPos(player.posX, player.posY - 1.0D, player.posZ);
                    Block block = mc.theWorld.getBlockState(below).getBlock();
                    if (block.isReplaceable(mc.theWorld, below)) {
                        this.safeWalk.startSneak();
                    }
                }
                if (ground != null && ground.intValue() <= this.settings.getExtend()) {
                    if (this.autoEnableTimer == null
                            || this.autoEnableTimer.lastMs + 40L <= System.currentTimeMillis()) {
                        this.placement = this.placementScanner.lastResult;
                        if (this.placement != null) {
                            this.lastPlacePos = this.placement.pos;
                            this.lastPlaceBox = this.placement.placedPos;
                        }
                    }
                }
            }
        }

        ScaffoldMode mode = this.settings.mode.getValue();
        boolean rotatedThisTick = false;
        if (this.isRotationWindowActive() && this.settings.isRotationsEnabled()) {
            boolean lookForward = mode.shouldLookForward();
            boolean airborne = mc.thePlayer.onGround || this.willLandOnBlock(mc.thePlayer.motionY) || lookForward;
            if (mc.thePlayer.onGround) {
                this.firstTurn = true;
            }
            boolean forward = this.shouldRotateForward(!airborne);
            if (forward && airborne) {
                RotationModel jumpModel = mode.getForwardRotationModel();
                Objects.requireNonNull(SprintModule.INSTANCE.rotateMode);
                rotateTowardsMovement(jumpModel, this.targetPitch, 0.3F);
                this.field132 = true;
                this.field131 = true;
            } else {
                if (!forward) {
                    this.field131 = false;
                }
                boolean snapGuard = this.S();
                if (!snapGuard || this.placement != null) {
                    this.k(snapGuard);
                    rotatedThisTick = true;
                }
            }
        }
        mode.onPostRotation();
        if (this.placeReady && !rotatedThisTick) {
            this.placement = this.placementFinder.recompute(this.placement, null);
        }
        if (this.autoEnableTimer != null && mc.thePlayer.onGround) {
            long now = System.currentTimeMillis();
            if (this.autoEnableTimer.lastMs + 250L <= now) {
                if (ground != null && ground.intValue() < 3) {
                    return;
                }
                Timer velocity = ServerTracker.INSTANCE.field116;
                if (velocity != null && velocity.lastMs + 650L <= System.currentTimeMillis()) {
                    this.autoEnableTimer = null;
                    this.setEnabled(false, true);
                }
            }
        }
    }

    @EventHandler
    public void onMoveInput(MovementInputEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        if (this.isRotationWindowActive()
                && this.noAscendActive
                && !this.placementTracker.hasPending()
                && !mc.thePlayer.capabilities.isFlying
                && this.settings.isAutoJump()
                && this.settings.mode.getValue().canAutoJump()) {
            mc.thePlayer.jumpTicks = 0;
            e.jump = true;
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent e) {
        if (!this.isEnabled() || !this.settings.isBlockPlacements()) {
            return;
        }
        if (this.lastPlaceBox == null || this.lastPlacePos == null) {
            return;
        }
        RenderUtil.begin3d();
        RenderManager rm = mc.getRenderManager();
        double viewX = rm.viewerPosX;
        double viewY = rm.viewerPosY;
        double viewZ = rm.viewerPosZ;
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

    private void handlePlacement(PlacementTarget placement) {
        if (this.settings.isWaitForClick()) {
            if (!Adjust.cpsCounter.canClick(this.settings.cps)) {
                return;
            }
        }
        this.executePlacement(placement);
        this.lastPlacement = placement;
        this.placementTracker.recordPlacementTime(placement.pos);
        ScaffoldMode mode = this.settings.mode.getValue();
        mode.onPlace(placement);
        this.placementTracker.trackPlacement(placement);
        this.settings.blockCountHud.onPlace(placement);
    }

    private static float updateJitter(boolean dryRun, float playerYaw, float targetYaw, float jitter) {
        float diff = MathHelper.wrapAngleTo180_float(playerYaw - targetYaw);
        if (Math.abs(diff) < 1.0F) {
            return 0.0F;
        }
        if (jitter < 10.0F) {
            return jitter + (dryRun ? 4.0F : RandomUtil.nextFloat(3.0F, 4.0F));
        }
        return jitter;
    }

    public int simulateRotationTicks(Vec2f target) {
        boolean snapGuard = this.S();
        EntityPlayerSP player = mc.thePlayer;
        boolean first = player.onGround || this.firstTurn;
        float jitter = this.rotationJitter;
        Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        float tolerance = Math.max(Math.ulp(target.x), Math.ulp(target.y));
        for (int ticks = 0; ticks < 4; ticks++) {
            RotationModel model = this.getRotationModel(snapGuard, first, true, current.x, target.x, jitter);
            if (!(model instanceof ScaffoldLinearRotationModel
                    || model instanceof ScaffoldWeightedRotationModel
                    || model instanceof LinearRotationModel
                    || model instanceof InstantRotationModel)) {
                return 0;
            }
            jitter = first ? 0.0F : updateJitter(true, current.x, target.x, jitter);
            first = false;
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
            if (tolerance >= Math.abs(yawError) && tolerance >= Math.abs(pitchError)) {
                return ticks;
            }
            current = next;
        }
        return 4;
    }

    boolean isAboveBuildLimit(Entity entity) {
        ScaffoldMode mode = this.settings.mode.getValue();
        int y = (int) entity.posY - 1;
        return mode.isAboveBuildLimit(y);
    }

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
        if (Adjust.gameStateTracker.state.game == HypixelGame.BED_WARS && block == Blocks.pumpkin) {
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
                || block instanceof BlockBasePressurePlate || block instanceof BlockBush) {
            return false;
        }
        if (block == Blocks.tnt) {
            BlockerState server = ServerTracker.INSTANCE.serverState;
            if (server != null && server.antiCheatModel != null && server.antiCheatModel.serverType == ServerType.MINEMEN_CLUB) {
                return false;
            }
            HypixelGameState game = Adjust.gameStateTracker.state;
            if (game.startTimer != null && (game.game == HypixelGame.BED_WARS || game.game == HypixelGame.BED_WARS_PRACTICE)) {
                return false;
            }
        }
        return block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() == 1.0D
                && block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() == 1.0D
                && block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() == 1.0D;
    }

    private RotationModel getRotationModel(boolean instant, boolean first, boolean dryRun,
                                           float playerYaw, float targetYaw, float jitter) {
        if (!dryRun) {
            RotationModel modeModel = this.settings.mode.getValue().getPlaceRotationModel();
            if (modeModel != null) {
                return modeModel;
            }
        }
        RotationModel model;
        if (this.T()) {
            float limit = first ? 73.0F : 43.0F;
            if (!first && Math.abs(MathHelper.wrapAngleTo180_float(playerYaw - targetYaw)) >= 1.0F) {
                limit -= jitter;
            }
            float sub = dryRun
                    ? (float) ServiceUtil.movementAdjustment + 1.0F
                    : RandomUtil.nextFloat(0.1F, (float) ServiceUtil.movementAdjustment + 1.0F);
            model = new ScaffoldLinearRotationModel(limit - sub);
        } else {
            RotationSetting rotation = this.settings.rotation;
            model = rotation.mode.getValue().factory.apply(rotation);
        }
        if (this.autoEnableTimer != null) {
            float speed = dryRun ? 20.0F : 20.0F - RandomUtil.nextFloat(0.1F, 1.0F);
            float diff = Math.abs(MathHelper.wrapAngleTo180_float(playerYaw - targetYaw));
            if (diff < 150.0F) {
                if (diff > 35.0F) {
                    speed += 30.0F;
                }
            } else {
                speed += 110.0F;
            }
            model = MovementUtil.getSpeed() < 0.7D
                    ? new ScaffoldLinearRotationModel((double) speed)
                    : InstantRotationModel.INSTANCE;
        }
        return instant ? InstantRotationModel.INSTANCE : model;
    }

    private static void rotateTowardsMovement(RotationModel model, float pitch, float yawSpread) {
        float yaw = RenderRotationTracker.getRotation().x;
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

    private void executePlacement(PlacementTarget placement) {
        Vec3 hit = placement.hitVec;
        mc.rightClickDelayTimer = 0;
        mc.objectMouseOver = new MovingObjectPosition(new Vec3(hit.xCoord, hit.yCoord, hit.zCoord), placement.facing,
                placement.pos);
        ServerTracker.INSTANCE.mouseOverOverridden = true;
        SwingMouseClick click = ((MouseClickHelper) Adjust.mouseClickHelper).right;
        click.clicks = Integer.valueOf(1);
        click.down = Boolean.TRUE;
        click.holdTicks = 0;
        if (!this.settings.isClientsideSwing()) {
            click.swing = false;
        }
        Adjust.cpsCounter.recordClick();
    }

    boolean shouldRotateForward(boolean checkSneak) {
        if (!MovementFixModule.INSTANCE.isEnabled()) {
            return false;
        }
        ScaffoldMode mode = this.settings.mode.getValue();
        if (!mode.allowForwardTurn()) {
            return false;
        }
        if (!this.settings.isAllowSprint()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        if (checkSneak) {
            if (player.movementInput != null && player.movementInput.sneak && !player.sleeping) {
                return false;
            }
        }
        AxisAlignedBB above = player.getEntityBoundingBox().offset(0.0D, 1.0D, 0.0D);
        if (BlockUtil.isColliding(above)) {
            return false;
        }
        if (!mc.gameSettings.keyBindJump.pressed) {
            if (!this.isNoAscendActive(false)) {
                return false;
            }
            if (!this.settings.isAutoJump()) {
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

    @Override
    public String getSuffix() {
        return this.settings.mode.getValue().getName();
    }

    private boolean selectBlock(boolean force) {
        InventoryPlayer inventory = mc.thePlayer.inventory;
        boolean usable = false;
        int held = inventory.m();
        if (held < 9 && held >= 0) {
            ItemStack stack = inventory.mainInventory[inventory.m()];
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                usable = this.isUsableBlock(inventory.m(), stack);
                if (usable && !Adjust.slotSpoofer.active) {
                    return true;
                }
            }
        }
        if (!this.settings.isChangeSlot()) {
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
        SlotSpoofer spoofer = Adjust.slotSpoofer;
        spoofer.slot = best.intValue();
        spoofer.active = true;
        spoofer.field8 = spoofer.field12;
        spoofer.field17 = false;
        spoofer.renderSpoofed = clientsideSwap;
        this.slotChanged = true;
        if (this.candidateSlot == null || this.candidateSlot.intValue() != best.intValue()) {
            this.candidateSlot = best;
        }
        return true;
    }

    private boolean isNoAscendActive(boolean retainAirborne) {
        if (this.autoEnableTimer != null) {
            return false;
        }
        if (!this.settings.isNoAscend()) {
            return false;
        }
        if (this.placementTracker != null && this.placementTracker.hasPending()) {
            return false;
        }
        ScaffoldMode mode = this.settings.mode.getValue();
        if (!mode.canNoAscend()) {
            return false;
        }
        EntityPlayerSP player = mc.thePlayer;
        boolean force = retainAirborne && this.noAscendActive && !player.onGround;
        if (!this.settings.isNoAscendActive(force)) {
            return false;
        }
        if (mode.towering) {
            return false;
        }
        if (this.settings.isTower() && this.towerKeyOnGround) {
            return false;
        }
        if (this.isAboveBuildLimit(player)) {
            return false;
        }
        if (!MovementUtil.isMoving() && !force) {
            return false;
        }
        return !player.isPotionActive(Potion.jump);
    }

    private void k(boolean instant) {
        EntityPlayerSP player = mc.thePlayer;
        RotationModel model = this.getRotationModel(instant, this.firstTurn, false,
                player.rotationYaw, this.targetYaw, this.rotationJitter);
        final Vec2f current = new Vec2f(player.rotationYaw, player.rotationPitch);
        if (this.T()) {
            if (this.firstTurn) {
                this.field131 = false;
                this.rotationJitter = 0.0F;
                this.firstTurn = false;
            } else {
                this.rotationJitter = updateJitter(false, mc.thePlayer.rotationYaw, this.targetYaw, this.rotationJitter);
            }
        } else {
            this.firstTurn = true;
        }
        boolean simple = model instanceof ScaffoldLinearRotationModel
                || model instanceof ScaffoldWeightedRotationModel
                || model instanceof LinearRotationModel
                || model instanceof InstantRotationModel;
        if (!simple) {
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

    @Override
    public void onEnable() {
        this.resetState();
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
        EntityPlayerSP player = mc.thePlayer;
        this.baseY = player == null ? Integer.MAX_VALUE : (int) player.posY;
    }

    public void holdInput() {
        if (this.inputOverride != null) {
            return;
        }
        this.inputOverride = Adjust.inboundPacketQueue.addHold(null, false);
    }

    @EventHandler
    public void onPacket(DirectionalPacketEvent e) {
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
                        VirtualFileSystem files = Adjust.INSTANCE.fileSystem;
                        if (!files.loading && ConfigurationService.configurationsAvailable
                                && ConfigurationService.configurationReceived && ServiceUtil.pendingTasksReady) {
                            files.markDirty();
                        }
                    }
                }
            }
        } else if (packet instanceof S23PacketBlockChange && this.isEnabled()) {
            final S23PacketBlockChange blockChange = (S23PacketBlockChange) packet;
            mc.addScheduledTask(() -> this.placementTracker.onBlockChange(blockChange));
        }
    }

    public void enableTemporarily() {
        if (this.isEnabled()) {
            return;
        }
        this.autoEnableTimer = new Timer();
        this.setEnabled(true, true);
    }

    public void releaseInput() {
        this.field136 = false;
        this.clickQueue.flush();
        PacketHold override = this.inputOverride;
        if (override != null) {
            InboundPacketQueue manager = Adjust.inboundPacketQueue;
            manager.processNow(() -> manager.removeHold(override));
            this.inputOverride = null;
        }
    }

    void onModeChange(ScaffoldMode previous) {
        this.releaseInput();
        if (previous != null) {
            previous.reset();
        }
        this.settings.mode.getValue().reset();
    }

    @Override
    public void onDisable() {
        this.resetState();
        this.autoEnableTimer = null;
        this.settings.mode.getValue().reset();
        KeyBinding sneak = mc.gameSettings.keyBindSneak;
        boolean down = GameSettings.isKeyDown(sneak);
        if (!down) {
            sneak.pressTime = 0;
        }
        sneak.pressed = down;
        this.safeWalk.sneaking = false;
        this.safeWalk.unsneakTicks = 0;
        this.releaseInput();
    }
}
