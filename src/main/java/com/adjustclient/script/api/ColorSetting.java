package com.adjustclient.script.api;

import com.adjustclient.script.api.Setting;

public final class ColorSetting
extends Setting {
    public native int get();

    public native void set(int var1);

    public native ColorSetting description(String var1);
}

