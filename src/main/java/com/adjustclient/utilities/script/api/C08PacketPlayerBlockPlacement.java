package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.BlockPos;
import com.adjustclient.utilities.script.api.ItemStack;
import com.adjustclient.utilities.script.api.Packet;

public final class C08PacketPlayerBlockPlacement
extends Packet {
    public BlockPos position;
    public int placedBlockDirection;
    public ItemStack stack;
    public float facingX;
    public float facingY;
    public float facingZ;

    public C08PacketPlayerBlockPlacement() {
    }

    public C08PacketPlayerBlockPlacement(ItemStack stackIn) {
    }

    public C08PacketPlayerBlockPlacement(BlockPos positionIn, int placedBlockDirectionIn, ItemStack stackIn, float facingXIn, float facingYIn, float facingZIn) {
    }

    private native BlockPos $script$read$position();

    private native void $script$write$position(BlockPos var1);

    private native int $script$read$placedBlockDirection();

    private native void $script$write$placedBlockDirection(int var1);

    private native ItemStack $script$read$stack();

    private native void $script$write$stack(ItemStack var1);

    private native float $script$read$facingX();

    private native void $script$write$facingX(float var1);

    private native float $script$read$facingY();

    private native void $script$write$facingY(float var1);

    private native float $script$read$facingZ();

    private native void $script$write$facingZ(float var1);
}

