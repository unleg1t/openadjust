package com.adjustclient.modules.impl.render;

import com.adjustclient.core.settings.ModeOption;

public enum CrosshairMode implements ModeOption {
    CROSS("Cross"), GAPPED("Gapped"), DOT("Dot");
    private final String displayName;
    CrosshairMode(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
