package com.adjustclient.module.impl.movement;

import com.adjustclient.setting.ModeOption;

public enum MovementRecorderMode implements ModeOption {
    PREDICT("Predict"), ALWAYS("Always");
    private final String displayName;
    MovementRecorderMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
