package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.AttackSlowdownEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.ItemSlowdownEvent;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.event.SprintEvent;
import com.adjustclient.event.SprintState;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.Simulation;
import com.adjustclient.util.SimulationPlayer;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.Objects;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public final class SprintModule extends Module {
    public static SprintModule INSTANCE;
    public static SprintModule INSTANCE_ALT;
    private static int cachedResetTick = -1;
    private static Boolean cachedResetResult;

    public final ModeSetting<SprintServerState> serverState;
    public final ExpandSetting omnidirectional;
    public final BooleanSetting omnidirectionalEnabled;
    public final ModeSetting<OmniSprintMode> omnidirectionalMode;
    public final ExpandSetting keepSprint;
    public final BooleanSetting keepSprintEnabled;
    public final ModeSetting<KeepSprintMode> keepSprintMode;
    public final ExpandSetting strafe;
    public final BooleanSetting strafeEnabled;
    public final BooleanSetting strafeGroundOnly;
    public final ExpandSetting ignore;
    public final BooleanSetting ignoreBlindness;
    public final BooleanSetting ignoreHunger;
    public final BooleanSetting ignoreUsingItem;
    public final RotateOmniSprintMode rotateMode;

    public SprintModule() {
        super("Sprint", Category.MOVEMENT);
        INSTANCE = this;
        serverState = new ModeSetting<>(this, "Server state", SprintServerState.DEFAULT, SprintServerState.values());
        serverState.setDescription("Controls when the server sees you sprinting.");
        omnidirectional = new ExpandSetting(this, "Omnidirectional");
        omnidirectional.enabledPredicate = new OmniSprintCondition(this);
        omnidirectionalEnabled = new BooleanSetting(omnidirectional, "Enabled", false, "Yes", "No");
        NormalOmniSprintMode normal = new NormalOmniSprintMode(this, omnidirectional);
        rotateMode = new RotateOmniSprintMode(this);
        omnidirectionalMode = new ModeSetting<>(omnidirectional, "Mode", normal, normal, rotateMode);
        omnidirectionalMode.addListener((oldValue, newValue) -> {
            if (isEnabled() && newValue != oldValue) {
                if (oldValue != null) ((OmniSprintMode) oldValue).onDisable();
                ((OmniSprintMode) newValue).onEnable();
            }
        });
        keepSprint = new ExpandSetting(this, "Keep sprint");
        keepSprintEnabled = new BooleanSetting(keepSprint, "Enabled", false, "Yes", "No");
        keepSprint.enabledPredicate = () -> keepSprintEnabled.isEnabled();
        VanillaKeepSprintMode vanilla = new VanillaKeepSprintMode(this, keepSprint);
        HypixelKeepSprintMode hypixel = new HypixelKeepSprintMode(this, keepSprint);
        keepSprintMode = new ModeSetting<>(keepSprint, "Mode", vanilla, vanilla, hypixel);
        keepSprintMode.addListener((oldValue, newValue) -> {
            if (isEnabled() && oldValue != newValue) {
                if (oldValue != null) ((KeepSprintMode) oldValue).onDisable();
                ((KeepSprintMode) newValue).onEnable();
            }
        });
        strafe = new ExpandSetting(this, "Strafe");
        strafe.enabledPredicate = new KeepSprintCondition(this);
        strafeEnabled = new BooleanSetting(strafe, "Enabled", false, "Yes", "No");
        strafeGroundOnly = new BooleanSetting(strafe, "Ground only", false);
        ignore = new ExpandSetting(this, "Ignore");
        ignore.enabledPredicate = new StrafeCondition(this);
        ignoreBlindness = new BooleanSetting(ignore, "Blindness", false);
        ignoreHunger = new BooleanSetting(ignore, "Hunger", false);
        ignoreUsingItem = new BooleanSetting(ignore, "Using item", false);
        keepSprint.getSettings().remove(keepSprintMode);
        keepSprint.getSettings().add(1, keepSprintMode);
        omnidirectional.getSettings().remove(omnidirectionalMode);
        omnidirectional.getSettings().add(1, omnidirectionalMode);
        keepSprintEnabled.addListener(() -> {
            if (isEnabled()) {
                KeepSprintMode mode = Objects.requireNonNull(keepSprintMode.getValue());
                if (keepSprintEnabled.isEnabled()) mode.onEnable();
                else mode.onDisable();
            }
        });
        omnidirectionalEnabled.addListener(() -> {
            if (isEnabled()) {
                OmniSprintMode mode = Objects.requireNonNull(omnidirectionalMode.getValue());
                if (omnidirectionalEnabled.isEnabled()) mode.onEnable();
                else mode.onDisable();
            }
        });
        INSTANCE_ALT = this;
    }

    public static boolean shouldResetSprint() {
        if (mc.thePlayer == null) return false;
        if (cachedResetTick == mc.thePlayer.ticksExisted && cachedResetResult != null) {
            return cachedResetResult;
        }
        cachedResetTick = mc.thePlayer.ticksExisted;
        cachedResetResult = Boolean.TRUE;
        if (ServerTracker.INSTANCE.field116.hasElapsed(500L)) {
            cachedResetResult = Boolean.FALSE;
            return false;
        }
        double withAttack = simulateBackwardSpeed(true);
        double withoutAttack = simulateBackwardSpeed(false);
        boolean result = withoutAttack > withAttack;
        cachedResetResult = result;
        return result;
    }

    public boolean isVelocitySprintResetActive() {
        return keepSprintEnabled.isEnabled() && isSprintResetEnabled() && shouldResetSprint();
    }

    public boolean isSprintResetEnabled() {
        KeepSprintMode mode = keepSprintMode.getValue();
        return mode instanceof HypixelKeepSprintMode || ((VanillaKeepSprintMode) mode).sprintReset.isEnabled();
    }

    private static double simulateBackwardSpeed(boolean attack) {
        Simulation simulation = new Simulation(mc.thePlayer);
        SimulationPlayer player = simulation.player;
        if (attack) {
            AttackSlowdownEvent event = new AttackSlowdownEvent(0.6D);
            if (VanillaKeepSprintMode.INSTANCE != null && VanillaKeepSprintMode.INSTANCE.isActive()) {
                VanillaKeepSprintMode.INSTANCE.onAttackSlowdown(event);
            }
            player.motionX *= event.factor;
            player.motionZ *= event.factor;
            player.setSprinting(false);
            player.sprintingTicksLeft = 0;
        }
        player.tick();
        double radians = mc.thePlayer.rotationYaw * 0.0174532925199433D;
        return player.motionX * Math.sin(radians) - Math.cos(radians) * player.motionZ;
    }

    @EventHandler
    public void onSprint(SprintEvent event) {
        if (isEnabled() && event.state == SprintState.BLINDNESS && ignoreBlindness.isEnabled()) {
            event.cancelled = true;
        }
    }

    @EventHandler(priority = 10)
    public void onSprintEligibility(SprintEligibilityEvent event) {
        if (!isEnabled()) return;
        mc.gameSettings.keyBindSprint.pressed = true;
        if (ignoreHunger.isEnabled()) event.sprintEligible = true;
    }

    @EventHandler
    public void onItemSlowdown(ItemSlowdownEvent event) {
        if (isEnabled() && ignoreUsingItem.isEnabled()) {
            event.allowSprinting = true;
        }
    }

    @EventHandler(priority = 1)
    public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        switch (serverState.getValue()) {
            case BLOCKED:
                event.sprinting = false;
                break;
            case VALID_ONLY:
                if (!WorldUtil.canSprint(false)) event.sprinting = false;
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = -1)
    public void onMoveFlying(MoveFlyingEvent event) {
        if (!isEnabled() || event.pre || !strafeEnabled.isEnabled()) return;
        if (!Features.SPRINT_STRAFE.isAllowed()) return;
        if (strafeGroundOnly.isEnabled() && !mc.thePlayer.onGround) return;
        if (MovementUtil.isMoving()) MovementUtil.setSpeed(MovementUtil.getSpeed());
    }

    @Override
    public void onEnable() {
        keepSprintMode.getValue().onEnable();
        omnidirectionalMode.getValue().onEnable();
    }

    @Override
    public void onDisable() {
        KeyBinding key = mc.gameSettings.keyBindSprint;
        key.pressed = GameSettings.isKeyDown(key);
        if (!key.pressed) key.pressTime = 0;
        keepSprintMode.getValue().onDisable();
        omnidirectionalMode.getValue().onDisable();
    }
}
