package com.adjustclient.module.impl.combat.killaura;

import com.adjustclient.setting.ModeOption;

public enum KillAuraMode implements ModeOption {
    SINGLE("Single"), SWITCH("Switch");
    private final String displayName;
    KillAuraMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
