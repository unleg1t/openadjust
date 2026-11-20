package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.core.settings.ModeOption;

public enum SprintServerState implements ModeOption {
    DEFAULT("Default"),
    BLOCKED("Blocked"),
    VALID_ONLY("Valid only");

    private final String displayName;

    SprintServerState(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
