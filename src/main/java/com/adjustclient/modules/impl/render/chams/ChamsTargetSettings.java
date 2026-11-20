package com.adjustclient.modules.impl.render.chams;

import com.adjustclient.modules.Module;
import com.adjustclient.core.settings.*;
import java.awt.Color;

public final class ChamsTargetSettings {
    public final ExpandSetting group;
    public final BooleanSetting enabled;
    public final ModeSetting<ChamsMode> mode;
    public final ColorSetting color;
    public final BooleanSetting throughWallsEnabled;
    public final ColorSetting throughWallsColor;

    public ChamsTargetSettings(Module parent, String label, boolean enabled, Color color) {
        group = new ExpandSetting(parent, label);
        this.enabled = new BooleanSetting(group, "Enabled", enabled, "Yes", "No");
        group.enabledPredicate = () -> this.enabled.isEnabled();
        mode = new ModeSetting<>(group, "Mode", ChamsMode.COLORED, ChamsMode.values());
        this.color = new ColorSetting(group, "Color", color, false, false, ColorSetting.Mode.PLAIN,
                () -> mode.getValue() != ChamsMode.COLORED);
        ExpandSetting throughWalls = new ExpandSetting(group, "Through walls",
                () -> mode.getValue() != ChamsMode.COLORED);
        throughWallsEnabled = new BooleanSetting(throughWalls, "Enabled", true, "Yes", "No");
        throughWalls.enabledPredicate = () -> throughWallsEnabled.isEnabled();
        Color occluded = new Color((int) (color.getRed() * 0.7), (int) (color.getGreen() * 0.7),
                (int) (color.getBlue() * 0.7), color.getAlpha());
        throughWallsColor = new ColorSetting(throughWalls, "Color", occluded, false, false, ColorSetting.Mode.PLAIN, null);
    }
}
