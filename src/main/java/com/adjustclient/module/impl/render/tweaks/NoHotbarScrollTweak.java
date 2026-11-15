package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class NoHotbarScrollTweak extends Tweak {
    public static NoHotbarScrollTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public NoHotbarScrollTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "No hotbar scroll", false);
        enabled.setDescription("Disables your scroll wheel for moving through your hotbar items");
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
}
