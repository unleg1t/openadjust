package com.adjustclient.module.impl.render;

import com.adjustclient.setting.ModeOption;

public enum FogMode implements ModeOption {
    VANILLA("Default"), SOFT("Soft"), CONSTANT("Constant");
    private final String displayName;
    FogMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
