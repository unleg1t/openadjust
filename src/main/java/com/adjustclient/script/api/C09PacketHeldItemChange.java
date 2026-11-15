package com.adjustclient.script.api;

import com.adjustclient.script.api.Packet;

public final class C09PacketHeldItemChange
extends Packet {
    public int slotId;

    public C09PacketHeldItemChange() {
    }

    public C09PacketHeldItemChange(int slotId) {
    }

    public native int getSlotId();

    private native int $script$read$slotId();

    private native void $script$write$slotId(int var1);
}

