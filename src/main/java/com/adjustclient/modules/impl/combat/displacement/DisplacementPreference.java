package com.adjustclient.modules.impl.combat.displacement;

import com.adjustclient.core.settings.ModeOption;

public enum DisplacementPreference implements ModeOption {
    LEFT("Left"), RIGHT("Right"), TOWARDS("Towards"), RANDOM("Random");
    private final String displayName;
    DisplacementPreference(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
