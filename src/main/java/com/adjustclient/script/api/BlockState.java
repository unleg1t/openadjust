package com.adjustclient.script.api;

import com.adjustclient.script.api.Block;

public final class BlockState {
    public static native BlockState fromId(int var0);

    public native int getId();

    public native Block getBlock();
}

