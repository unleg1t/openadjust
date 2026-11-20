package com.adjustclient.core.rotation;

import java.util.function.Function;
import com.adjustclient.core.settings.ModeOption;
import com.adjustclient.utilities.RandomUtil;

public enum RotationMode implements ModeOption {
    LINEAR("Linear",
            setting -> getRandomSpeed(setting)),
    DISTRIBUTED("Distributed",
            setting -> DistributedRotationModel.INSTANCE),
    STEP("Step",
            setting -> createSmoothModel(setting)),
    ORGANIC("Organic",
            setting -> new OrganicRotationModel(setting.getMaxAngle(), setting.getDriftIntensity(), setting.getJitterIntensity())),
    INSTANT("Instant",
            setting -> InstantRotationModel.INSTANCE);

    public final String displayName;
    public final Function<RotationSetting, RotationModel> factory;

    RotationMode(String displayName, Function<RotationSetting, RotationModel> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    @Override public String getName() { return displayName; }

    public static RotationModel createSmoothModel(RotationSetting setting) {
        float maxAngle = setting.getMaxAngle();
        float stepAngle = setting.getStepAngle();
        return new RandomizedLinearRotationModel(maxAngle, Math.min(Math.max((double) maxAngle - 5.0, 5.0), (double) stepAngle));
    }

    public static RotationModel getRandomSpeed(RotationSetting setting) {
        return new LinearRotationModel(setting.getMaxAngle() + RandomUtil.nextFloat(0.01F, 0.05F));
    }

}
