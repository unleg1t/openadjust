package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.NumberFormat;
import com.adjustclient.utilities.script.api.Setting;

public final class NumberSetting
extends Setting {
    public native double get();

    public native void set(double var1);

    public native int intValue();

    public native long longValue();

    public native float floatValue();

    public native double doubleValue();

    public native NumberSetting formatter(NumberFormat var1);

    public native NumberSetting description(String var1);
}

