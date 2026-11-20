package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Setting;

public final class ModeSetting
extends Setting {
    public native String get();

    public native void set(String var1);

    public native ModeSetting option(String var1);

    public native ModeSetting description(String var1);
}

