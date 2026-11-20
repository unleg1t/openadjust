package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.BlockPos;
import com.adjustclient.utilities.script.api.DiggingAction;
import com.adjustclient.utilities.script.api.EnumFacing;
import com.adjustclient.utilities.script.api.Packet;

public final class C07PacketPlayerDigging
extends Packet {
    public BlockPos position;
    public EnumFacing facing;
    public DiggingAction status;

    public C07PacketPlayerDigging() {
    }

    public C07PacketPlayerDigging(DiggingAction statusIn, BlockPos posIn, EnumFacing facingIn) {
    }

    private native BlockPos $script$read$position();

    private native void $script$write$position(BlockPos var1);

    private native EnumFacing $script$read$facing();

    private native void $script$write$facing(EnumFacing var1);

    private native DiggingAction $script$read$status();

    private native void $script$write$status(DiggingAction var1);
}

