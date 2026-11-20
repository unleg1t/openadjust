package com.adjustclient.modules.impl.render.esp.settings;

import com.adjustclient.core.settings.ModeOption;

public enum EspTheme implements ModeOption {
    MINECRAFT("Minecraft"), PIXEL("Pixel"), ADJUST("Adjust");

    private final String name;
    public final boolean pixel;
    public final boolean adjust;
    private Object field24;

    EspTheme(String name) {
        this.name = name;
        pixel = !name.equals("Adjust");
        adjust = false;
    }

    @Override public String getName() { return name; }
}
