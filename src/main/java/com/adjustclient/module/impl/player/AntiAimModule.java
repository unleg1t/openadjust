package com.adjustclient.module.impl.player;

import com.adjustclient.anticheat.AntiCheat;
import com.adjustclient.anticheat.feature.Features;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.PreClientTickEvent;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.setting.*;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.util.anticheat.blocker.FeatureBlocker;
import java.util.function.BooleanSupplier;

public final class AntiAimModule extends Module {
    public static AntiAimModule INSTANCE;
    public final ModeSetting<YawMode> yaw;
    public final ModeSetting<PitchMode> pitch;
    public final NumberSetting spinSpeed;
    public final ExpandSetting disableOn;
    public final BooleanSetting disableOnRotations;
    public float spinYaw;
    public float spinPitch;

    public AntiAimModule() {
        super("Anti Aim", Category.PLAYER);
        INSTANCE = this;
        yaw = new ModeSetting<>(this, "Yaw", YawMode.BACKWARDS, YawMode.values());
        pitch = new ModeSetting<>(this, "Pitch", PitchMode.DOWN, PitchMode.values());
        spinSpeed = new NumberSetting(this, "Spin Speed", 10.0F, 1.0F, 180.0F, 0.5F,
                () -> yaw.getValue() != YawMode.SPIN && pitch.getValue() != PitchMode.SPIN);
        disableOn = new ExpandSetting(this, "Disable on");
        disableOnRotations = new BooleanSetting(disableOn, "Rotations", true);
        disableOnRotations.setDescription("Prevents replacing rotations from other modules.");
        disableOn.enabledPredicate = new DisableOnCondition(this);
        spinYaw = RandomUtil.nextFloat(4.0F, 150.0F);
        spinPitch = RandomUtil.nextFloat(4.0F, 150.0F);
    }

    public float advanceSpin(float previous) {
        return previous + spinSpeed.getValue().floatValue() + RandomUtil.nextFloat(0.0F, 0.2F);
    }

    @EventHandler(priority = 32) public void onRotation(IngameActionEvent event) {
        if (!isEnabled() || disableOnRotations.isEnabled()) return;
        applyRotation();
    }

    @EventHandler(priority = 500) public void onDeferredRotation(PreClientTickEvent event) {
        if (isEnabled() && mc.thePlayer != null && disableOnRotations.isEnabled()) applyRotation();
    }

    public void applyRotation() {
        if (mc.thePlayer == null) return;
        if (yaw.getValue() == YawMode.DISABLED && pitch.getValue() == PitchMode.DISABLED) return;
        FeatureBlocker blocker = ServerTracker.INSTANCE.serverState == null ? null
                : ServerTracker.INSTANCE.serverState.featureBlocker;
        if (AntiCheatModule.restrictionsEnabled && blocker != null) {
            for (AntiCheat antiCheat : blocker.antiCheats.values()) {
                if (!antiCheat.isAllowed(Features.ANTI_AIM)) {
                    blocker.reportBlocked(antiCheat, Features.ANTI_AIM);
                    return;
                }
            }
        }
        float nextYaw = mc.thePlayer.rotationYaw;
        float nextPitch = mc.thePlayer.rotationPitch;
        switch (yaw.getValue()) {
            case BACKWARDS: nextYaw -= 180.0F; break;
            case SPIN: spinYaw = advanceSpin(spinYaw) % 360.0F; nextYaw = spinYaw; break;
            case RANDOM: nextYaw = RandomUtil.nextFloat(0.0F, 360.0F); break;
            default: break;
        }
        switch (pitch.getValue()) {
            case DOWN: nextPitch = 90.0F; break;
            case UP: nextPitch = -90.0F; break;
            case DOWN_HEADLESS: nextPitch = -180.0F; break;
            case UP_HEADLESS: nextPitch = 180.0F; break;
            case SPIN: spinPitch = advanceSpin(spinPitch) % 179.0F; nextPitch = spinPitch - 90.0F; break;
            case RANDOM: nextPitch = RandomUtil.nextFloat(0.0F, 179.89999F) - 90.0F; break;
            default: break;
        }
        if (Math.abs(nextPitch) > 90.0F) {
        }
        RotationHandler.INSTANCE.target = new Vec2f(nextYaw, nextPitch);
        RotationHandler.INSTANCE.model = InstantRotationModel.INSTANCE;
        RotationHandler.INSTANCE.requested = true;
        RotationHandler.INSTANCE.active = true;
        RotationHandler.release();
        if (Math.abs(nextPitch) > 90.0F) RotationHandler.INSTANCE.field19 = true;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.service.telemetry.BypassFeature.ANTIAIM_HEADLESS);
    }

    static class DisableOnCondition implements BooleanSupplier {
        final AntiAimModule owner;
        DisableOnCondition(AntiAimModule owner) { this.owner = owner; }
        public boolean getAsBoolean() { return owner.disableOnRotations != null && owner.disableOnRotations.isEnabled(); }
    }

    public enum PitchMode implements ModeOption {
        DISABLED("Disabled"), DOWN("Down"), UP("Up"), DOWN_HEADLESS("Down Headless"),
        UP_HEADLESS("Up Headless"), SPIN("Spin"), RANDOM("Random");
        private final String displayName;
        PitchMode(String displayName) { this.displayName = displayName; }
        public String getName() { return displayName; }
    }

    public enum YawMode implements ModeOption {
        DISABLED("Disabled"), BACKWARDS("Backwards"), SPIN("Spin"), RANDOM("Random");
        private final String displayName;
        YawMode(String displayName) { this.displayName = displayName; }
        public String getName() { return displayName; }
    }
}
