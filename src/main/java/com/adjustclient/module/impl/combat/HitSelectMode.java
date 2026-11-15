package com.adjustclient.module.impl.combat;

import com.adjustclient.setting.ModeOption;

enum HitSelectMode implements ModeOption {
    BURST("Burst"), CRITICAL("Critical");
    private final String displayName;
    HitSelectMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
