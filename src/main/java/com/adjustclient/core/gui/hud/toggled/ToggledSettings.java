package com.adjustclient.core.gui.hud.toggled;

import com.adjustclient.core.settings.BooleanSetting;
import com.adjustclient.core.settings.ExpandSetting;
import com.adjustclient.core.settings.ModeSetting;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class ToggledSettings {

    public BooleanSetting enabled;
    public ModeSetting<ToggledColor> color;
    public BooleanSetting background;
    public BooleanSetting suffix;
    public ModeSetting<ToggledBorder> border;
    public ModeSetting<ToggledPadding> padding;
    public ExpandSetting group;

    public ToggledSettings(ExpandSetting group, BooleanSupplier headerEnabled) {
        this.group = group;
        group.enabledPredicate = headerEnabled;
        this.enabled = new BooleanSetting(group, "Enabled", true, "Yes", "No");
        this.background = new BooleanSetting(group, "Background", true, "On", "Off");
        this.suffix = new BooleanSetting(group, "Suffix", true, "On", "Off");
        this.color = new ModeSetting<>(group, "Color", ToggledColor.DEFAULT, List.of(ToggledColor.values()));
        this.border = new ModeSetting<>(group, "Border", ToggledBorder.FULL, List.of(ToggledBorder.values()));
        this.padding = new ModeSetting<>(group, "Padding", ToggledPadding.NORMAL, List.of(ToggledPadding.values()));
    }
}
