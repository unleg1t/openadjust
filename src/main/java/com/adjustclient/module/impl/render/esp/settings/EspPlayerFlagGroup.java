package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.setting.*;
import java.awt.Color;

public final class EspPlayerFlagGroup {
    public final BooleanSetting enabled;
    public final ColorSetting color;
    public EspPlayerFlagGroup(SettingContainer parent, String name, Color defaultColor, boolean enabledDefault) {
        ExpandSetting group = new ExpandSetting(parent, name);
        enabled = new BooleanSetting(group, "Enabled", enabledDefault, "Yes", "No");
        group.enabledPredicate = this::isEnabled;
        color = new ColorSetting(group, "Color", defaultColor);
    }
    public boolean isEnabled() { return enabled.isEnabled(); }
}
