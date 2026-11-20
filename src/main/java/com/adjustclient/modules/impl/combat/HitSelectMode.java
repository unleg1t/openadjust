package com.adjustclient.modules.impl.combat;

import com.adjustclient.core.settings.ModeOption;

enum HitSelectMode implements ModeOption {
    BURST("Burst"), CRITICAL("Critical");
    private final String displayName;
    HitSelectMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
