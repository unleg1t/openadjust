package com.adjustclient.modules.impl.render.blockoverlay;

import com.adjustclient.core.settings.*;
import java.awt.Color;

public class RangedOverlaySettings extends BlockOverlaySettings {
    public final NumberSetting maxRange;
    public RangedOverlaySettings(String name, SettingContainer parent, Color color, boolean fullDefault, Runnable changed) {
        super(name, parent, color, false, fullDefault, changed);
        maxRange = new NumberSetting(group, "Max. range", 65, 6, 65, 1).formatter(
                setting -> setting.getValue().intValue() == setting.getMax().intValue() ? "Infinite" : Integer.toString(setting.getValue().intValue()));
    }
}
