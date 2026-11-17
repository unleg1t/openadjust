package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.event.EventHandler;
import com.adjustclient.event.ViewBobbingEvent;
import com.adjustclient.setting.BooleanSetting;

public final class MinimalBobbingTweak extends Tweak {
    public static MinimalBobbingTweak INSTANCE;
    public final BooleanSetting enabled;
    public Object field12;

    public MinimalBobbingTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        enabled = new BooleanSetting(module, "Minimal bobbing", false);
        enabled.setDescription("Removes the camera shake from view bobbing.");
    }

    @EventHandler
    public void onViewBobbing(ViewBobbingEvent event) {
        if (!isEnabled()) return;
        if (event.type == ViewBobbingEvent.Type.WORLD) {
            event.setCancelled(true);
        }
    }

    @Override public boolean enabledSetting() { return enabled.isEnabled(); }
}
