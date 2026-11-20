package com.adjustclient.modules.impl.render.bedplates;

import com.adjustclient.core.settings.ModeOption;

public enum BedPlatesMode implements ModeOption {
    BLOCKS("Blocks"), LAYERS("Layers");
    private final String displayName;
    BedPlatesMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
