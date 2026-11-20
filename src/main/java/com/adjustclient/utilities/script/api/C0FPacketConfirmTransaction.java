package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class C0FPacketConfirmTransaction
extends Packet {
    public int windowId;
    public short uid;
    public boolean accepted;

    public C0FPacketConfirmTransaction() {
    }

    public C0FPacketConfirmTransaction(int windowId, short uid, boolean accepted) {
    }

    private native int $script$read$windowId();

    private native void $script$write$windowId(int var1);

    private native short $script$read$uid();

    private native void $script$write$uid(short var1);

    private native boolean $script$read$accepted();

    private native void $script$write$accepted(boolean var1);
}

