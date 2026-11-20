package com.adjustclient.modules.impl.combat;
import com.adjustclient.core.settings.ModeOption;
public enum TPAuraMode implements ModeOption {
    HOUSING("Housing");
    private final String displayName;
    TPAuraMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
