package com.adjustclient.script.api;

import com.adjustclient.script.api.ItemStack;
import com.adjustclient.script.api.Packet;

public final class S04PacketEntityEquipment
extends Packet {
    public int entityID;
    public int equipmentSlot;
    public ItemStack itemStack;

    public S04PacketEntityEquipment() {
    }

    public S04PacketEntityEquipment(int entityIDIn, int p_i45221_2_, ItemStack itemStackIn) {
    }

    private native int $script$read$entityID();

    private native void $script$write$entityID(int var1);

    private native int $script$read$equipmentSlot();

    private native void $script$write$equipmentSlot(int var1);

    private native ItemStack $script$read$itemStack();

    private native void $script$write$itemStack(ItemStack var1);
}

