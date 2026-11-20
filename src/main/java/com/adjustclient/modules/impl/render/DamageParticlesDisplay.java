package com.adjustclient.modules.impl.render;

import com.adjustclient.core.settings.ModeOption;

public enum DamageParticlesDisplay implements ModeOption {
    ALWAYS("Always"), DAMAGE_ONLY("Damage only");
    private final String displayName;
    DamageParticlesDisplay(String displayName) { this.displayName = displayName; }
    @Override public String getName() { return displayName; }
}
