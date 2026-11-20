package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.ModeOption;

public enum EspIconLocation implements ModeOption {
    ICON("Icon"), NAMETAG("Nametag");

    private final String name;
    private Object field20;

    EspIconLocation(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }
}
