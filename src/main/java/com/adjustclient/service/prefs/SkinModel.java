package com.adjustclient.service.prefs;

import com.adjustclient.setting.ModeOption;

public enum SkinModel implements ModeOption {
    EVERYONE("Everyone"),
    FRIENDS("Friends"),
    DISABLED("Disabled");

    SkinModel(String displayName) { this.displayName = displayName; }
    public String displayName;
    public Object field20;

    public String getName() { return displayName; }
}
