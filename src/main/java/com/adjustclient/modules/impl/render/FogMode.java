package com.adjustclient.modules.impl.render;

import com.adjustclient.core.settings.ModeOption;

public enum FogMode implements ModeOption {
    VANILLA("Default"), SOFT("Soft"), CONSTANT("Constant");
    private final String displayName;
    FogMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
