package com.adjustclient.module.impl.movement;

import com.adjustclient.setting.ModeOption;

public enum TerrainMode implements ModeOption {
    LEGIT("Legit", null),
    VANILLA("Vanilla", com.adjustclient.anticheat.feature.Features.TERRAIN_VANILLA),
    VULCAN("Vulcan", com.adjustclient.anticheat.feature.Features.TERRAIN_VULCAN),
    HYPIXEL("Hypixel", null);

    private final String displayName;
    public final com.adjustclient.anticheat.feature.ModuleFeature field20;

    TerrainMode(String displayName, com.adjustclient.anticheat.feature.ModuleFeature field20) {
        this.displayName = displayName;
        this.field20 = field20;
    }

    @Override public String getName() { return displayName; }

}
