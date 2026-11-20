package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.ModeOption;

public enum EspBorderMode implements ModeOption {
    DISABLED("Disabled"), CORNERED("Cornered"), FULL("Full");

    private final String name;
    private Object field20;

    EspBorderMode(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }
}
