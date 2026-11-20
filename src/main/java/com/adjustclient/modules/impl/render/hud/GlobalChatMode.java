package com.adjustclient.modules.impl.render.hud;

import com.adjustclient.core.settings.ModeOption;

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
