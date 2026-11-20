package com.adjustclient.modules.impl.render.indicators;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;

public class IndicatorSettings {
    public final ExpandSetting group;
    public final BooleanSetting enabled;
    public final NumberSetting alpha;
    public final ExpandSetting name;
    public final BooleanSetting nameEnabled;
    public final BooleanSetting distance;

    public IndicatorSettings(Module parent, String label, boolean enabled) {
        group = new ExpandSetting(parent, label);
        this.enabled = new BooleanSetting(group, "Enabled", enabled, "Yes", "No");
        group.enabledPredicate = () -> this.enabled.isEnabled();
        alpha = new NumberSetting(group, "Alpha", 255, 70, 255, 1);
        name = new ExpandSetting(group, "Name");
        nameEnabled = new BooleanSetting(name, "Enabled", true, "Yes", "No");
        name.enabledPredicate = () -> nameEnabled.isEnabled();
        distance = new BooleanSetting(group, "Distance", false, "On", "Off");
    }
    public int getAlpha() { return alpha.getInt(); }
    public boolean isEnabled() { return enabled.isEnabled(); }
    public boolean isNameEnabled() { return nameEnabled.isEnabled(); }
    public boolean isDistanceEnabled() { return distance.isEnabled(); }
}
