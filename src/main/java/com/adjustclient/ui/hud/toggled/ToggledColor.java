package com.adjustclient.ui.hud.toggled;

import com.adjustclient.setting.ModeOption;

public enum ToggledColor implements ModeOption {
    DEFAULT("Default"),
    RAINBOW("Rainbow"),
    ASTOLFO("Astolfo"),
    RANDOM("Random");

    private final String name;

    ToggledColor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
