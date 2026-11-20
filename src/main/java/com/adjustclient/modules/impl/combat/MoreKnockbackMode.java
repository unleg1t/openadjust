package com.adjustclient.modules.impl.combat;
import com.adjustclient.core.settings.ModeOption;
public enum MoreKnockbackMode implements ModeOption {
    W_TAP("W Tap"), SNEAK("Sneak"), NO_STOP("No Stop"), SILENT("Silent");
    private final String displayName;
    MoreKnockbackMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
