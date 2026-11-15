package com.adjustclient.module.impl.movement;

import com.adjustclient.setting.ModeOption;

public enum DoubleJumpMode implements ModeOption {
    VANILLA("Vanilla"), MMC("MMC");
    private final String displayName;
    DoubleJumpMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
