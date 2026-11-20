package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Block;

public final class Item {
    public static native Item getItemById(int var0);

    public static native Item getItemFromBlock(Block var0);

    public static native int getIdFromItem(Item var0);

    public native String getUnlocalizedName();
}

