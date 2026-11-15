package com.adjustclient.rotation;

import java.util.function.BooleanSupplier;
import com.adjustclient.setting.ModeSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.setting.SettingContainer;

public final class RotationSetting {
    public BooleanSupplier dependency;
    public ModeSetting<RotationMode> mode;
    public NumberSetting maxAngle;
    public NumberSetting stepAngle;
    public NumberSetting driftIntensity;
    public NumberSetting jitterIntensity;

    public RotationSetting(SettingContainer parent, RotationMode defaultMode, BooleanSupplier dependency, RotationMode... modes) {
        this.dependency = dependency != null ? dependency :
                () -> false;
        this.mode = new ModeSetting<>(parent, "Mode", defaultMode, this.dependency, modes);
        maxAngle = new NumberSetting(parent, "Max. angle", 60.0F, 10.0F, 180.0F, 1.0F,
                () -> this.dependency.getAsBoolean() || mode.getValue() == RotationMode.DISTRIBUTED || mode.getValue() == RotationMode.INSTANT);
        maxAngle.setDescription("Maximum angle distance allowed.");
        stepAngle = new NumberSetting(parent, "Step angle", 25.0F, 5.0F, 180.0F, 1.0F,
                () -> this.dependency.getAsBoolean() || mode.getValue() != RotationMode.STEP);
        stepAngle.setDescription("The amount to rotate if your angle distance exceeds max. angle.");
        driftIntensity = new NumberSetting(parent, "Drift intensity", 1.2, 0.5, 2, 0.1,
                () -> this.dependency.getAsBoolean() || mode.getValue() != RotationMode.ORGANIC);
        jitterIntensity = new NumberSetting(parent, "Jitter intensity", 0.12, 0, 0.3, 0.01,
                () -> this.dependency.getAsBoolean() || mode.getValue() != RotationMode.ORGANIC);
        maxAngle.formatter(NumberSetting.INTEGER_SUFFIX_A);
        stepAngle.formatter(NumberSetting.INTEGER_SUFFIX_A);
    }
    public float getMaxAngle() { return maxAngle.getValue().floatValue(); }
    public float getStepAngle() { return stepAngle.getValue().floatValue(); }
    public float getDriftIntensity() { return driftIntensity.getValue().floatValue(); }
    public float getJitterIntensity() { return jitterIntensity.getValue().floatValue(); }
    public RotationModel createModel() { return mode.getValue().factory.apply(this); }
}
