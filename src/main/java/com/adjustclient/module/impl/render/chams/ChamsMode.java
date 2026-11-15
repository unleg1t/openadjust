package com.adjustclient.module.impl.render.chams;

import com.adjustclient.setting.ModeOption;

public enum ChamsMode implements ModeOption {
    NORMAL("Normal"), COLORED("Colored");
    private final String name;
    ChamsMode(String name) { this.name = name; }
    @Override public String getName() { return name; }
}
