package com.adjustclient.script.api;

public final class GuiScreen {
    public int width;
    public int height;
    public boolean allowUserInput;

    public native boolean doesGuiPauseGame();

    private native int $script$read$width();

    private native void $script$write$width(int var1);

    private native int $script$read$height();

    private native void $script$write$height(int var1);

    private native boolean $script$read$allowUserInput();

    private native void $script$write$allowUserInput(boolean var1);
}

