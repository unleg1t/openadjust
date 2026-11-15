package com.adjustclient.module.impl.render;

import com.adjustclient.setting.ModeOption;

public enum LightingMode implements ModeOption {
    DEFAULT("Default"), BRIGHT("Bright"), CUSTOM("Custom");
    private final String displayName;
    LightingMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
