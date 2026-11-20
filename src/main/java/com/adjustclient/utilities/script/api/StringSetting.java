package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Setting;

public final class StringSetting
extends Setting {
    public native String get();

    public native void set(String var1);

    public native StringSetting description(String var1);
}

