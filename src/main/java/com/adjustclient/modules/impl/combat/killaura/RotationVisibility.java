package com.adjustclient.modules.impl.combat.killaura;

import com.adjustclient.core.settings.ModeOption;

public enum RotationVisibility implements ModeOption {
    SILENT("Silent"), LOCK_ON("Lock on"), LINEAR("Linear");
    private final String displayName;
    RotationVisibility(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
