package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class C00PacketKeepAlive
extends Packet {
    public int key;

    private native int $script$read$key();

    private native void $script$write$key(int var1);
}

