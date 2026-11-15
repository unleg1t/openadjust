package com.adjustclient.module.impl.render.blockoverlay;

import com.adjustclient.setting.*;
import java.awt.Color;

public final class ChestOverlaySettings extends RangedOverlaySettings {
    public final BooleanSetting hideOpened;
    public ChestOverlaySettings(SettingContainer parent, Color color, Runnable changed) {
        super("Chest", parent, color, true, changed);
        ExpandSetting hypixel = new ExpandSetting(group, "Hypixel");
        hideOpened = new BooleanSetting(hypixel, "Hide opened", true);
    }
}
