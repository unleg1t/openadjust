package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.setting.*;
import java.awt.Color;

public class BorderedEspSettings extends EntityEspSettings {
    public final ExpandSetting border;
    public final ModeSetting<EspBorderMode> borderMode;
    public final ColorSetting borderColor;

    public BorderedEspSettings(SettingContainer parent, String name, boolean enabled, boolean dormant) {
        super(parent, name, enabled, dormant);
        border = new ExpandSetting(target, "Border");
        borderMode = new ModeSetting<>(border, "Mode", EspBorderMode.DISABLED, EspBorderMode.values());
        border.enabledPredicate = () -> borderMode.getValue() != EspBorderMode.DISABLED;
        borderColor = new ColorSetting(border, "Color", new Color(170, 170, 170), false, false, ColorSetting.Mode.PLAIN, null);
    }
}
