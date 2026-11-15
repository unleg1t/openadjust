package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class NoTitleTweak extends Tweak {
    public static NoTitleTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public NoTitleTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "No title", false);
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
}
