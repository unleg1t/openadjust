package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ItemStack;

public final class SlotHelper {
    public static native void setSlot(int var0);

    public static native void setSlot(int var0, boolean var1, boolean var2);

    public static native void reset();

    public static native boolean isActive();

    public static native boolean isSilent();

    public static native ItemStack getRenderItemStack();

    public static native int getRenderItemSlot();
}

