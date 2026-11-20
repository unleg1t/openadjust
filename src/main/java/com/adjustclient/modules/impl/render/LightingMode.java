package com.adjustclient.modules.impl.render;

import com.adjustclient.core.settings.ModeOption;

public enum LightingMode implements ModeOption {
    DEFAULT("Default"), BRIGHT("Bright"), CUSTOM("Custom");
    private final String displayName;
    LightingMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
