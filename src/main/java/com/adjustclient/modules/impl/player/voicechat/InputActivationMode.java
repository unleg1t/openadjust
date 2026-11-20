package com.adjustclient.modules.impl.player.voicechat;

import com.adjustclient.core.settings.ModeOption;

public enum InputActivationMode implements ModeOption {
    PUSH("Push"), DETECT("Detect"), ALWAYS("Always");
    public final String displayName;
    InputActivationMode(String displayName) { this.displayName = displayName; }
    public String getName() { return displayName; }
}
