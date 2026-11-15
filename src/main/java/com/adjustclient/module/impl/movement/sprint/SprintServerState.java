package com.adjustclient.module.impl.movement.sprint;

import com.adjustclient.setting.ModeOption;

public enum SprintServerState implements ModeOption {
    DEFAULT("Default"),
    BLOCKED("Blocked"),
    VALID_ONLY("Valid only");

    private final String displayName;

    SprintServerState(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
