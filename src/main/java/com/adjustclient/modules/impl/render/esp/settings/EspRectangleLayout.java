package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.ModeOption;

public enum EspRectangleLayout implements ModeOption {
    TOGETHER("Together"), SEPARATED("Separated");

    private final String name;
    private Object field20;

    EspRectangleLayout(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }
}
