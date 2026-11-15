package com.adjustclient.ui.hud.tabgui;

import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;

import java.util.function.BooleanSupplier;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.ui.tabgui.TabGui;

public final class TabGuiSettings {

    public final Object owner;
    public final BooleanSetting enabled;
    public final BooleanSetting gradient;
    public final BooleanSetting autoClose;
    public int width = 49;
    public int rowHeight = 8;
    public Object field28;

    public TabGuiSettings(Object owner, HUDModule hud) {
        this.width = 49;
        this.rowHeight = 8;
        this.owner = owner;

        // new ExpandSetting(hud, "Tab GUI"); added to hud.getSettings()
        ExpandSetting group = new ExpandSetting(hud, "Tab GUI");
        this.enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        group.enabledPredicate = (BooleanSupplier) () -> this.enabled.isEnabled();
        this.gradient = new BooleanSetting(group, "Gradient", true, "On", "Off");
        this.autoClose = new BooleanSetting(group, "Auto Close", true, "On", "Off");
    }
}
