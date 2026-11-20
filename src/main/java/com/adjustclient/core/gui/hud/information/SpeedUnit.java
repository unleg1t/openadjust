package com.adjustclient.core.gui.hud.information;

import com.adjustclient.core.settings.ModeOption;

public enum SpeedUnit implements ModeOption {
    BPS("BPS"),
    KMPH("KMPH");

    private final String name;

    SpeedUnit(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
