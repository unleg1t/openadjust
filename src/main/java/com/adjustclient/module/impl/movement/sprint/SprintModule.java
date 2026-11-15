package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.AttackSlowdownEvent;
import com.adjustclient.event.CancellablePlayerUpdateEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.MotionEvent;
import com.adjustclient.event.MoveFlyingEvent;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.SprintEvent;
import com.adjustclient.event.SprintState;
import com.adjustclient.event.SprintEligibilityEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.util.MovementUtil;
import com.adjustclient.util.WorldUtil;
import java.util.Objects;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;

public final class SprintModule extends Module {
    public static SprintModule INSTANCE;
    public static SprintModule INSTANCE_ALT;
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
    public final RotateOmniSprintMode rotateMode;
    public float velocityYaw;
    public int velocityTicks;

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
        HypixelKeepSprintMode hypixel = new HypixelKeepSprintMode(this);
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

    @EventHandler public void onUpdate(CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled()) return;
        if (velocityTicks >= 1) velocityTicks--;
    }

    @EventHandler(priority = -1) public void onAttackSlowdown(AttackSlowdownEvent event) {
        if (isEnabled() && isVelocitySprintResetActive()) event.factor = 0.6D;
    }

    public boolean isVelocitySprintResetActive() {
        return keepSprintEnabled.isEnabled() && isSprintResetEnabled() && velocityTicks >= 1;
    }

    @EventHandler public void onSprint(SprintEvent event) {
        if (isEnabled() && event.state == SprintState.BLINDNESS && ignoreBlindness.isEnabled()) event.cancelled = true;
    }

    @EventHandler(priority = 10) public void onSprintEligibility(SprintEligibilityEvent event) {
        if (!isEnabled()) return;
        mc.gameSettings.keyBindSprint.pressed = true;
        if (ignoreHunger.isEnabled()) event.sprintEligible = true;
    }

    @EventHandler(priority = 1) public void onMotion(MotionEvent event) {
        if (!isEnabled() || !event.pre) return;
        switch (serverState.getValue()) {
            case BLOCKED: event.sprinting = false; break;
            case VALID_ONLY: if (!WorldUtil.canSprint(false)) event.sprinting = false; break;
            default: break;
        }
    }

    @EventHandler public void onMoveFlyingReset(MoveFlyingEvent event) {
        if (!isEnabled() || event.pre || !isVelocitySprintResetActive()) return;
        double x = mc.thePlayer.motionX == -0.0D ? 0.0D : mc.thePlayer.motionX;
        double z = mc.thePlayer.motionZ == -0.0D ? 0.0D : mc.thePlayer.motionZ;
        float yaw = (float) (Math.atan2(-x, z) * 57.29577951308232D);
        if (Math.abs(MathHelper.wrapAngleTo180_float(yaw - velocityYaw)) >= 120.0F) velocityTicks = 0;
    }

    @EventHandler(priority = -1) public void onMoveFlying(MoveFlyingEvent event) {
        if (!isEnabled() || event.pre || !strafeEnabled.isEnabled()) return;
        if (!Features.SPRINT_STRAFE.isAllowed()) return;
        if (strafeGroundOnly.isEnabled() && !mc.thePlayer.onGround) return;
        if (MovementUtil.isMoving()) MovementUtil.setSpeed(MovementUtil.getSpeed());
    }

    @Override public void onEnable() {
        keepSprintMode.getValue().onEnable();
        omnidirectionalMode.getValue().onEnable();
    }

    @Override public void onDisable() {
        KeyBinding key = mc.gameSettings.keyBindSprint;
        key.pressed = GameSettings.isKeyDown(key);
        if (!key.pressed) key.pressTime = 0;
        keepSprintMode.getValue().onDisable();
        omnidirectionalMode.getValue().onDisable();
        velocityTicks = 0;
    }

    @EventHandler public void onPacket(DirectionalPacketEvent event) {
        if (event.isCancelled() || !isEnabled() || !keepSprintEnabled.isEnabled() || !isSprintResetEnabled() || event.outgoing || mc.thePlayer == null) return;
        if (!(event.packet instanceof S12PacketEntityVelocity)) return;
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.packet;
        if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;
        int x = packet.getMotionX(), y = packet.getMotionY(), z = packet.getMotionZ();
        if (x == 0 && y == 0 && z == 0) return;
        velocityTicks = 10;
        double motionX = x / 8000.0D, motionZ = z / 8000.0D;
        if (motionX == -0.0D) motionX = 0.0D;
        if (motionZ == -0.0D) motionZ = 0.0D;
        velocityYaw = (float) (Math.atan2(-motionX, motionZ) * 57.29577951308232D);
    }

    public boolean isSprintResetEnabled() {
        KeepSprintMode mode = keepSprintMode.getValue();
        return mode instanceof HypixelKeepSprintMode || ((VanillaKeepSprintMode) mode).sprintReset.isEnabled();
    }
}
