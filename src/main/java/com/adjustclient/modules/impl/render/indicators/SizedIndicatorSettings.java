package com.adjustclient.modules.impl.render.indicators;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;

public class SizedIndicatorSettings extends IndicatorSettings {
    public final ColorSetting color;
    public final NumberSetting size;

    public SizedIndicatorSettings(Module parent, String label, boolean enabled, java.awt.Color defaultColor) {
        super(parent, label, enabled);
        color = new ColorSetting(group, "Color", defaultColor);
        color.alpha = false;
        size = new NumberSetting(group, "Size", 7.0f, 1.0f, 15.0f, 0.5f);
        group.getSettings().remove(name);
        group.getSettings().remove(distance);
        group.getSettings().add(name);
        group.getSettings().add(distance);
    }
    public float getSize() { return size.getFloat(); }
}
