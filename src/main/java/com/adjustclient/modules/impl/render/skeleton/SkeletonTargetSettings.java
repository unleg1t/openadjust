package com.adjustclient.modules.impl.render.skeleton;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ColorSetting;
import com.adjustclient.core.settings.ExpandSetting;
import java.awt.Color;

class SkeletonTargetSettings {
    public ExpandSetting group;
    public BooleanSetting enabled;
    public ColorSetting color;

    public SkeletonTargetSettings(Module parent, String name) {
        group = new ExpandSetting(parent, name);
        group.enabledPredicate = () -> enabled.isEnabled();
        enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        color = new ColorSetting(group, "Color", new Color(0x96f5f5f5, true));
    }

    public int getColor() { return color.getColor().getRGB(); }
}
