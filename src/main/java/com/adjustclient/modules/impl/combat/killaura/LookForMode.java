package com.adjustclient.modules.impl.combat.killaura;

import com.adjustclient.core.settings.ModeOption;

public enum LookForMode implements ModeOption {
    ONE_TICK("One Tick"), INSTANT("Zero Ticks"), DISABLED("Disabled");
    private final String displayName;
    LookForMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
