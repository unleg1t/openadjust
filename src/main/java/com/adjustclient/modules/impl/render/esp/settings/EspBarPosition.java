package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.ModeOption;

public enum EspBarPosition implements ModeOption {
    BOTTOM("Bottom"), RIGHT("Right");

    private final String name;
    private Object field20;

    EspBarPosition(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }
}
