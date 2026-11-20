package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Setting;

public final class KeybindSetting
extends Setting {
    public native boolean isDown();

    public native boolean isEmpty();

    public native KeybindSetting description(String var1);
}

