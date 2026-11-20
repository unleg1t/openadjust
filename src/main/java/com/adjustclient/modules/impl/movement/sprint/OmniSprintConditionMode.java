package com.adjustclient.modules.impl.movement.sprint;

import com.adjustclient.core.settings.ModeOption;

public enum OmniSprintConditionMode implements ModeOption {
    ALWAYS("Always"),
    AIR_ONLY("Only in air"),
    GROUND_ONLY("Only on ground"),
    NO_JUMP("Not when jumping");

    private final String displayName;

    OmniSprintConditionMode(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
