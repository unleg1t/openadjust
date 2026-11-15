package com.adjustclient.script.api;

import com.adjustclient.script.api.Setting;

public class ItemsSetting
extends Setting {
    public native boolean contains(int var1);

    public native void add(int var1);

    public native void remove(int var1);

    public native ItemsSetting description(String var1);
}

