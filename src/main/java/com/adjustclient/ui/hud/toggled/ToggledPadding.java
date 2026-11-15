package com.adjustclient.ui.hud.toggled;

import com.adjustclient.setting.ModeOption;

public enum ToggledPadding implements ModeOption {
    NORMAL("Normal", 8.0F),
    COMPACT("Compact", 7.5F);

    private final String name;
    public final float rowHeight;

    ToggledPadding(String name, float rowHeight) {
        this.name = name;
        this.rowHeight = rowHeight;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
