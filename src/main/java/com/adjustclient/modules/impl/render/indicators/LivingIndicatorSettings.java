package com.adjustclient.modules.impl.render.indicators;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;

public class LivingIndicatorSettings extends SizedIndicatorSettings {
    public final BooleanSetting healthEnabled;
    public final BooleanSetting customColorEnabled;
    public final ColorSetting healthColor;

    public LivingIndicatorSettings(Module parent, String label, boolean enabled, java.awt.Color defaultColor) {
        super(parent, label, enabled, defaultColor);
        ExpandSetting health = new ExpandSetting(group, "Health");
        healthEnabled = new BooleanSetting(health, "Enabled", false, "Yes", "No");
        health.enabledPredicate = () -> healthEnabled.isEnabled();
        ExpandSetting custom = new ExpandSetting(health, "Custom color");
        customColorEnabled = new BooleanSetting(custom, "Enabled", true, "Yes", "No");
        custom.enabledPredicate = () -> customColorEnabled.isEnabled();
        healthColor = new ColorSetting(custom, "Color", new java.awt.Color(67, 90, 209), false, false, ColorSetting.Mode.PLAIN, null);
    }
    public java.awt.Color getHealthColor() { return healthColor.getColor(); }
}
