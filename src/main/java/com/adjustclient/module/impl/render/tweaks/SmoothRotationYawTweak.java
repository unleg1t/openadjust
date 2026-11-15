package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class SmoothRotationYawTweak extends Tweak {
    public static SmoothRotationYawTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public SmoothRotationYawTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "Smooth rotation yaw", false);
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
}
