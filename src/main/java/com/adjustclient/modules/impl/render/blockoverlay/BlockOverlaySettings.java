package com.adjustclient.modules.impl.render.blockoverlay;

import com.adjustclient.core.settings.*;
import java.awt.Color;

public class BlockOverlaySettings {
    public final ExpandSetting group;
    public final BooleanSetting full;
    public final ColorSetting fullColor;
    public final BooleanSetting outline;
    public final ColorSetting outlineColor;
    public final NumberSetting thickness;

    public BlockOverlaySettings(String name, SettingContainer parent, Color color, boolean outlineDefault, boolean fullDefault, Runnable changed) {
        group = new ExpandSetting(parent, name);
        group.enabledPredicate = () -> isVisible();
        full = new BooleanSetting(group, "Full", fullDefault, "On", "Off");
        if (changed != null) full.addListener(changed);
        fullColor = new ColorSetting(group, "Full color", color);
        outline = new BooleanSetting(group, "Outline", outlineDefault, "On", "Off");
        if (changed != null) outline.addListener(changed);
        outlineColor = new ColorSetting(group, "Outline color", new Color((int)(color.getRed() * 0.7), (int)(color.getGreen() * 0.7), (int)(color.getBlue() * 0.7), color.getAlpha()));
        thickness = new NumberSetting(group, "Thickness", 1.0f, 1.0f, 5.0f, 0.1f,
                () -> !outline.isEnabled());
    }

    public boolean isVisible() {
        return full.isEnabled() && fullColor.getColor().getAlpha() != 0
                || outline.isEnabled() && fullColor.getColor().getAlpha() != 0;
    }

    public float getThickness() { return thickness.getValue().floatValue(); }
}
