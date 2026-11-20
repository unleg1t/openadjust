package com.adjustclient.modules.impl.movement;

import com.adjustclient.core.settings.ModeOption;

public enum EagleMode implements ModeOption {
    STATIC("Static"),
    AUTOMATIC("Automatic");

    private final String displayName;

    EagleMode(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
