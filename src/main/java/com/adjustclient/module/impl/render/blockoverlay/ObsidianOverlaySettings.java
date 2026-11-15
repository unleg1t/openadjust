package com.adjustclient.module.impl.render.blockoverlay;

import com.adjustclient.setting.*;
import java.awt.Color;

public final class ObsidianOverlaySettings extends RangedOverlaySettings {
    public final BooleanSetting onlySurroundingBeds;
    public ObsidianOverlaySettings(SettingContainer parent, Color color) {
        super("Obsidian", parent, color, false, null);
        onlySurroundingBeds = new BooleanSetting(group, "Only surrounding beds", true);
    }
}
