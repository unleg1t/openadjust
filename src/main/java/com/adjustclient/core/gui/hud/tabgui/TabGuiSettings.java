package com.adjustclient.core.gui.hud.tabgui;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;

import java.util.function.BooleanSupplier;
import com.adjustclient.modules.impl.render.hud.HUDModule;
import com.adjustclient.core.gui.tabgui.TabGui;

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
