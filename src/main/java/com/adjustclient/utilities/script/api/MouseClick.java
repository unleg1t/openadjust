package com.adjustclient.utilities.script.api;

public class MouseClick {
    public native boolean isDisabled();

    public native void setDisabled();

    public native boolean isDown();

    public native boolean isClicked();

    public native int holdTicks();

    public native void click(int var1);

    public native void hold(int var1);

    public native void release();
}

