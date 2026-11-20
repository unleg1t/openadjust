package com.adjustclient.modules.impl.movement;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventHandler;
import com.adjustclient.core.events.MovementInputEvent;
import com.adjustclient.modules.Category;
import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;
import com.adjustclient.core.settings.NumberSetting;
import com.adjustclient.utilities.BlockUtil;
import com.adjustclient.utilities.Cooldown;
import com.adjustclient.utilities.MovementUtil;
import com.adjustclient.utilities.Simulation;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.ScreenUtil;
import com.adjustclient.utilities.Vec2f;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.core.rotation.handler.RenderRotationTracker;
import com.adjustclient.utilities.server.ServerType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import java.util.Random;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import com.adjustclient.utilities.game.player.InventoryStateTracker;

public final class SafeWalkModule extends Module {
    public static SafeWalkModule INSTANCE;
    public final ExpandSetting eagle;
    public final BooleanSetting eagleEnabled;
    public final ModeSetting<EagleMode> eagleMode;
    public final NumberSetting earlySneak;
    public final NumberSetting unsneakDelay;
    public final BooleanSetting noSneakAnimation;
    public final BooleanSetting avoidDoubleSneak;
    private Random random;
    private final Timer randomTimer;
    public Cooldown cooldown;
    public final ExpandSetting validate;
    public final BooleanSetting validateRotationYaw;
    public final BooleanSetting validateRotationPitch;
    public int sneakTicks;
    public boolean sneaking;

    public SafeWalkModule() {
        super("Safe Walk", Category.MOVEMENT);
        INSTANCE = this;
        eagle = new ExpandSetting(this, "Eagle");
        eagleEnabled = new BooleanSetting(eagle, "Enabled", false, "Yes", "No");
        eagle.enabledPredicate = () -> eagleEnabled.isEnabled();
        eagleMode = new ModeSetting<>(eagle, "Mode", EagleMode.STATIC, EagleMode.values());
        earlySneak = new NumberSetting(eagle, "Early sneak", 0, 0, 3, 1,
                () -> eagleMode.getValue() == EagleMode.AUTOMATIC).formatter(NumberSetting.TICKS);
        unsneakDelay = new NumberSetting(eagle, "Un-sneak delay", 0, 0, 4, 1,
                () -> eagleMode.getValue() == EagleMode.AUTOMATIC).formatter(NumberSetting.TICKS);
        noSneakAnimation = new BooleanSetting(eagle, "No sneak animation", false);
        avoidDoubleSneak = new BooleanSetting(eagle, "Avoid double sneak", false);
        randomTimer = new Timer();
        validate = new ExpandSetting(this, "Validate");
        validate.enabledPredicate = new SafeWalkEagleCondition(this);
        validateRotationYaw = new BooleanSetting(validate, "Rotation Yaw", true);
        validateRotationPitch = new BooleanSetting(validate, "Rotation Pitch", true);
        INSTANCE = this;
        eagleEnabled.addListener(() -> { sneakTicks = 0; sneaking = false; });
        eagleMode.addListener(() -> { sneakTicks = 0; sneaking = false; });
    }

    @EventHandler(priority = 44)
    public void onIngameAction(com.adjustclient.core.events.IngameActionEvent event) {
        if (!isEnabled() && cooldown == null) return;
        validatePlacement();
    }

    public void validatePlacement() {
        if (!avoidDoubleSneak.isEnabled() || mc.thePlayer == null || mc.theWorld == null || !sneaking) return;
        if (mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindJump.isKeyDown()) return;
        if (!mc.thePlayer.onGround || mc.thePlayer.onGroundTicks < 2 || mc.thePlayer.motionY >= 0.0D
                || (cooldown == null && (!isActive() || !isEagleActive()))
                || (!MovementUtil.hasMoved() && !MovementUtil.isMoving())) return;
        MovingObjectPosition hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || hit.sideHit == null || hit.sideHit.getIndex() < 2) return;
        ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
        if (stack == null || !(stack.getItem() instanceof ItemBlock)) return;
        ItemBlock item = (ItemBlock) stack.getItem();
        if (!item.getBlock().isFullCube()) return;
        BlockPos pos = hit.getBlockPos();
        var block = BlockUtil.getBlock(pos);
        if (block == null) return;
        if (!block.isReplaceable(mc.theWorld, pos)) pos = pos.offset(hit.sideHit);
        if (pos.getY() != MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY - 0.01D)) return;
        if (!item.canPlaceBlockOnSide(mc.theWorld, hit.getBlockPos(), hit.sideHit, mc.thePlayer, stack)) return;
        AxisAlignedBB placedBox = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        Simulation walking = new Simulation();
        walking.simulateTick(mc.thePlayer, true);
        walking.player.movementInput.jump = false;
        walking.player.field692 = placedBox;
        float[] input = MovementInputEvent.fromKeyboard().getMovement();
        int early = getSneakDelay(earlySneak);
        int remaining = sneakTicks;
        int edgeTick = early + 2;
        int releaseDelay = getSneakDelay(unsneakDelay);
        if (cooldown != null && !cooldown.isExpired()) releaseDelay = 1;
        Simulation prediction = new Simulation();
        for (int tick = 0; tick < releaseDelay + sneakTicks + edgeTick + 2; tick++) {
            boolean touchesPlaced = placedBox.intersectsWith(walking.player.getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D));
            boolean wasSneaking = remaining >= 1;
            int nextRemaining = wasSneaking ? remaining - 1 : remaining;
            prediction.simulateTick(walking.player, true);
            prediction.player.movementInput.jump = false;
            prediction.player.movementInput.moveForward = input[0];
            prediction.player.movementInput.moveStrafe = input[1];
            prediction.player.field692 = placedBox;
            for (int ahead = 0; ahead < early + 4; ahead++) {
                if (ahead == edgeTick && (remaining >= 1 || !touchesPlaced)) break;
                prediction.player.tick();
                if (!prediction.player.onGround) {
                    if (ahead >= edgeTick) {
                        Adjust.mouseClickHelper.right.disabled = true;
                        return;
                    }
                    wasSneaking = true;
                    nextRemaining = releaseDelay;
                    break;
                }
                touchesPlaced |= placedBox.intersectsWith(prediction.player.getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D));
            }
            if (!wasSneaking) return;
            walking.player.movementInput.moveForward = 0.3F * input[0];
            walking.player.movementInput.moveStrafe = 0.3F * input[1];
            walking.player.setSneaking(true);
            walking.player.tick();
            if (!walking.player.onGround) return;
            remaining = nextRemaining;
        }
    }

    public void updateSneaking() {
        // Preserve the entry snapshot: expiry clears the field, but this invocation still uses its captured hold.
        Cooldown hold = cooldown;
        if (!eagleEnabled.isEnabled() && !MovementFixModule.INSTANCE.isEnabled() && hold == null) return;
        sneaking = false;
        if (hold != null && hold.isExpired()) {
            cooldown = null;
            sneakTicks = 0;
            sneaking = false;
        }
        if (!mc.thePlayer.onGround || mc.thePlayer.onGroundTicks < 2 || mc.thePlayer.motionY >= 0.0D
                || (hold == null && (!isActive() || !isEagleActive()))
                || (!MovementUtil.hasMoved() && !MovementUtil.isMoving())) {
            sneakTicks = 0;
            return;
        }
        if (sneakTicks >= 1) {
            --sneakTicks;
            sneaking = true;
        }
        Simulation simulation = new Simulation();
        simulation.simulateTick(mc.thePlayer, true);
        int lookAhead = getSneakDelay(earlySneak) + 2;
        for (int tick = 0; tick < lookAhead; tick++) {
            simulation.player.tick();
            if (!simulation.player.onGround) {
                sneaking = true;
                sneakTicks = getSneakDelay(unsneakDelay);
                if (hold != null) sneakTicks = 1;
                return;
            }
        }
    }

    public int getSneakDelay(NumberSetting setting) {
        if (eagleMode.getValue() != EagleMode.AUTOMATIC) return setting.getInt();
        if (random == null || (random.nextDouble() > (double) 0.95F && randomTimer.hasElapsed(4500L))) {
            random = new Random();
            randomTimer.reset();
        }
        return RandomUtil.nextInt(random, setting.getMin().intValue(), setting.getMax().intValue() + 1);
    }

    @EventHandler public void onMovementInput(MovementInputEvent event) {
        if ((!isEnabled() && cooldown == null) || !sneaking) return;
        if (InventoryStateTracker.inServerGui) {
            sneaking = false;
            return;
        }
        if (!InventoryStateTracker.inServerInventory && !ScreenUtil.isSettingsScreenOpen()) event.sneak = true;
        else sneaking = false;
    }

    @Override public void onEnable() { sneakTicks = 0; sneaking = false; }

    @Override public void onDisable() { sneakTicks = 0; sneaking = false; }

    @Override public String getDescription() { return "Prevents you from walking off edges, similar to sneaking."; }

    public boolean isEagleActive() { return eagleEnabled.isEnabled() || MovementFixModule.INSTANCE.isEnabled(); }

    public boolean isActive() {
        if (!isEnabled() || ScaffoldModule.INSTANCE.isEnabled()) return false;
        var blocker = ServerTracker.INSTANCE.serverState;
        if (blocker != null && blocker.antiCheatModel != null
                && blocker.antiCheatModel.serverType == ServerType.HYPIXEL) {
            var state = Adjust.gameStateTracker.state;
            if (state.lobby == null || state.lobby || state.isSpectator()) return false;
        }
        if (validateRotationYaw.isEnabled()) {
            float playerYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
            Vec2f rotation = RenderRotationTracker.INSTANCE.rotation;
            float direction = MathHelper.wrapAngleTo180_float(rotation == null ? mc.thePlayer.rotationYaw : rotation.x);
            float forward = mc.gameSettings.keyBindForward.isKeyDown() ? 1.0F : 0.0F;
            if (mc.gameSettings.keyBindBack.isKeyDown()) forward -= 1.0F;
            float strafe = mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0F : 0.0F;
            if (mc.gameSettings.keyBindRight.isKeyDown()) strafe -= 1.0F;
            float factor = 1.0F;
            if (forward < 0.0F) {
                direction += 180.0F;
                factor = -0.5F;
            } else if (forward > 0.0F) factor = 0.5F;
            if (strafe > 0.0F) direction -= factor * 90.0F;
            if (strafe < 0.0F) direction += factor * 90.0F;
            if (Math.abs(direction - playerYaw) < 90.0F) return false;
        }
        return !validateRotationPitch.isEnabled() || mc.thePlayer.rotationPitch > 72.0F;
    }

    @EventHandler public void onSafeWalk(com.adjustclient.core.events.SafeWalkEvent event) {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.onGround && isActive() && !MovementFixModule.INSTANCE.isEnabled())
            event.safeWalk = true;
    }

    public boolean shouldHideSneakAnimation() {
        return eagleEnabled.isEnabled() && noSneakAnimation.isEnabled() && !mc.gameSettings.keyBindSneak.isKeyDown();
    }
}
