package com.adjustclient.module.impl.render.trajectories.settings;

import com.adjustclient.module.impl.render.trajectories.ProjectileType;
import com.adjustclient.setting.*;
import java.awt.Color;
import java.util.EnumMap;
import java.util.function.BooleanSupplier;

public final class ProjectileColorSettings {
    public final BooleanSetting enabled;
    public final EnumMap<ProjectileType, ColorSetting> colors;

    public ProjectileColorSettings(SettingContainer parent) {
        ExpandSetting group = new ExpandSetting(parent, "Projectile Colors");
        enabled = new BooleanSetting(group, "Enabled", true);
        enabled.setDescription("Override trajectory colors by projectile type.");
        BooleanSupplier hidden = () -> !enabled.isEnabled();
        colors = new EnumMap<>(ProjectileType.class);
        int[] rgb = {0x96ff8014, 0x96ebebeb, 0xa07828be, 0x96cde6ff, 0x96e6d8aa, 0x96cd5ac8};
        for (ProjectileType type : ProjectileType.values())
            colors.put(type, new ColorSetting(group, type.displayName, new Color(rgb[type.ordinal()], true), hidden));
    }

    public int getColor(ProjectileType type) {
        return colors.get(type).getColor().getRGB();
    }
}
