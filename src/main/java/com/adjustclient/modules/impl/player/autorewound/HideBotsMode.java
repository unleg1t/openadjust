package com.adjustclient.modules.impl.player.autorewound;

import com.adjustclient.core.settings.ModeOption;

public enum HideBotsMode implements ModeOption {
    NEVER("Never"),
    ALWAYS("Always"),
    PIT_ONLY("Pit only");

    private final String name;

    HideBotsMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
