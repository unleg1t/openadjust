package com.adjustclient.script.api;

import com.adjustclient.script.api.ItemStack;
import com.adjustclient.script.api.Packet;

public final class C10PacketCreativeInventoryAction
extends Packet {
    public int slotId;
    public ItemStack stack;

    public C10PacketCreativeInventoryAction() {
    }

    public C10PacketCreativeInventoryAction(int slotIdIn, ItemStack stackIn) {
    }

    private native int $script$read$slotId();

    private native void $script$write$slotId(int var1);

    private native ItemStack $script$read$stack();

    private native void $script$write$stack(ItemStack var1);
}

