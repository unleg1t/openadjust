package com.adjustclient.core.gui.hud.toggled;

import com.adjustclient.core.settings.ModeOption;

public enum ToggledBorder implements ModeOption {
    FULL("Full"),
    RIGHT("Right"),
    NONE("None");

    private final String name;

    ToggledBorder(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
