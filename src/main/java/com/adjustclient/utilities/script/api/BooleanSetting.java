package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Setting;

public final class BooleanSetting
extends Setting {
    public native boolean get();

    public native void set(boolean var1);

    public native BooleanSetting description(String var1);
}

