package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class NoBossBarTweak extends Tweak {
    public static NoBossBarTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public NoBossBarTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "No boss bar", false);
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
}
