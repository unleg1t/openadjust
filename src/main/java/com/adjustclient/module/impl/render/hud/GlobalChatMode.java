package com.adjustclient.module.impl.render.hud;

import com.adjustclient.setting.ModeOption;

public enum GlobalChatMode implements ModeOption {
    ALWAYS("Always"),
    TAB("Tab Only"),
    DISABLED("Disabled");

    private final String label;

    GlobalChatMode(String label) {
        this.label = label;
    }

    @Override
    public String getName() {
        return this.label;
    }

}
