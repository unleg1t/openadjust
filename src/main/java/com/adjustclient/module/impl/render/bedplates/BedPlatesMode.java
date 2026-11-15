package com.adjustclient.module.impl.render.bedplates;

import com.adjustclient.setting.ModeOption;

public enum BedPlatesMode implements ModeOption {
    BLOCKS("Blocks"), LAYERS("Layers");
    private final String displayName;
    BedPlatesMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
