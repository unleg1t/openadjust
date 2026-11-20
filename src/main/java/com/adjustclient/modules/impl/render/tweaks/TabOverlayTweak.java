package com.adjustclient.modules.impl.render.tweaks;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;

public final class TabOverlayTweak extends Tweak {
    public static TabOverlayTweak INSTANCE;
    public final BooleanSetting removeNpcs;

    public TabOverlayTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        ExpandSetting settings = new ExpandSetting(module, "Tab Overlay");
        settings.enabledPredicate = this::isRemoveNpcsActive;
        removeNpcs = new BooleanSetting(settings, "Remove NPCs", true);
        removeNpcs.setDescription("Hide NPC entities from showing in your tab player list.");
    }
    private boolean isRemoveNpcsActive() { return removeNpcs.getAsBoolean(); }
    @Override public boolean enabledSetting() { return module.isEnabled() && removeNpcs.isEnabled(); }
}
