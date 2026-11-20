package com.adjustclient.modules.impl.render.blockoverlay;

import com.adjustclient.core.settings.*;
import java.awt.Color;

public class SpawnerOverlaySettings extends RangedOverlaySettings {
    public final BooleanSetting activatedOnly;
    public SpawnerOverlaySettings(SettingContainer parent, Color color, Runnable changed) {
        super("Mob Spawner", parent, color, false, changed);
        activatedOnly = new BooleanSetting(group, "Activated Only", false);
        activatedOnly.setDescription("Only renders spawners that have had a player near them.");
    }
}
