package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ItemStack;
import com.adjustclient.utilities.script.api.Packet;

public final class S2FPacketSetSlot
extends Packet {
    public int windowId;
    public int slot;
    public ItemStack item;

    public S2FPacketSetSlot() {
    }

    public S2FPacketSetSlot(int windowIdIn, int slotIn, ItemStack itemIn) {
    }

    private native int $script$read$windowId();

    private native void $script$write$windowId(int var1);

    private native int $script$read$slot();

    private native void $script$write$slot(int var1);

    private native ItemStack $script$read$item();

    private native void $script$write$item(ItemStack var1);
}

