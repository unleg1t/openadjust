package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.BlockPos;
import com.adjustclient.utilities.script.api.Packet;

public final class S25PacketBlockBreakAnim
extends Packet {
    public int breakerId;
    public BlockPos position;
    public int progress;

    public S25PacketBlockBreakAnim() {
    }

    public S25PacketBlockBreakAnim(int breakerId, BlockPos pos, int progress) {
    }

    private native int $script$read$breakerId();

    private native void $script$write$breakerId(int var1);

    private native BlockPos $script$read$position();

    private native void $script$write$position(BlockPos var1);

    private native int $script$read$progress();

    private native void $script$write$progress(int var1);
}

