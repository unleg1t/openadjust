package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Setting;

public final class HotbarSetting
extends Setting {
    public native int get(int var1);

    public native void set(int var1, int var2);

    public native HotbarSetting description(String var1);
}

