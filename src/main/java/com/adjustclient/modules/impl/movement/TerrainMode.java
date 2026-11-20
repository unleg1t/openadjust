package com.adjustclient.modules.impl.movement;

import com.adjustclient.core.settings.ModeOption;

public enum TerrainMode implements ModeOption {
    LEGIT("Legit", null),
    VANILLA("Vanilla", com.adjustclient.utilities.anticheat.feature.Features.TERRAIN_VANILLA),
    VULCAN("Vulcan", com.adjustclient.utilities.anticheat.feature.Features.TERRAIN_VULCAN),
    HYPIXEL("Hypixel", null);

    private final String displayName;
    public final com.adjustclient.utilities.anticheat.feature.ModuleFeature field20;

    TerrainMode(String displayName, com.adjustclient.utilities.anticheat.feature.ModuleFeature field20) {
        this.displayName = displayName;
        this.field20 = field20;
    }

    @Override public String getName() { return displayName; }

}
