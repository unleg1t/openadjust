package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class C0DPacketCloseWindow
extends Packet {
    public int windowId;

    public C0DPacketCloseWindow() {
    }

    public C0DPacketCloseWindow(int windowId) {
    }

    private native int $script$read$windowId();

    private native void $script$write$windowId(int var1);
}

