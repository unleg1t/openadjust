package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.setting.BooleanSetting;

public final class DisconnectConfirmTweak extends Tweak {
    public static DisconnectConfirmTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public DisconnectConfirmTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "Disconnect confirm", false);
        enabled.setDescription("Requires two consecutive clicks on the disconnect button to leave");
    }

    @Override public boolean enabledSetting() { return module.isEnabled() && enabled.isEnabled(); }
}
