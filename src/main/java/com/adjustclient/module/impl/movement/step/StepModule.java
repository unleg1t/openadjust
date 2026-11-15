package com.adjustclient.module.impl.movement.step;

import com.adjustclient.Adjust;
import com.adjustclient.bind.Bind;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.setting.KeybindSetting;
import com.adjustclient.setting.ModeSetting;

public final class StepModule extends Module {
    public static StepModule INSTANCE;
    public final ModeSetting<StepMode> mode;
    public final KeybindSetting disableBind;

    public StepModule() {
        super("Step", Category.MOVEMENT);
        INSTANCE = this;
        Bind disableBind = Adjust.INSTANCE.bindManager.register("module.step.disablebind", ignored -> {
        }, "Step Disable Bind");
        this.disableBind = new KeybindSetting(this, "Disable Bind", disableBind);
        this.disableBind.setDescription("Step will temporarily stop working when this key is held.");
        VanillaStepMode vanilla = new VanillaStepMode(this);
        NcpStepMode ncp = new NcpStepMode(this);
        NcpStepMode.INSTANCE = ncp;
        MotionStepMode motion = new MotionStepMode(this);
        mode = new ModeSetting<>(this, "Mode", vanilla, vanilla, ncp, motion);
        mode.addListener((oldMode, newMode) -> {
            if (oldMode instanceof MotionStepMode previous) {
                previous.result = null;
                previous.lastAlignedTick = -1;
            }
        });
        // The factory creates Height before Mode, then moves Mode to the front of the settings list.
        settings.remove(mode);
        settings.add(0, mode);
    }

    @Override public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        if (mode.getValue() instanceof MotionStepMode motion) {
            motion.result = null;
            motion.lastAlignedTick = -1;
        }
    }

    @Override public String getSuffix() { return mode.getValue().getName(); }

    public boolean isStepping() {
        if (!isEnabled()) return false;
        StepMode selected = mode.getValue();
        if (selected instanceof MotionStepMode motion) return motion.result != null || motion.cooldownTicks >= 1;
        if (selected instanceof NcpStepMode ncp) return ncp.isStepping();
        return false;
    }
}
