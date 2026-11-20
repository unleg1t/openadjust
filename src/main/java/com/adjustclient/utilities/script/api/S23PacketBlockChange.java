package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.BlockPos;
import com.adjustclient.utilities.script.api.BlockState;
import com.adjustclient.utilities.script.api.Packet;
import com.adjustclient.utilities.script.api.World;

public final class S23PacketBlockChange
extends Packet {
    public BlockPos blockPosition;
    public BlockState blockState;

    public S23PacketBlockChange() {
    }

    public S23PacketBlockChange(World worldIn, BlockPos blockPositionIn) {
    }

    public S23PacketBlockChange(BlockPos blockPosition, BlockState blockState) {
    }

    private native BlockPos $script$read$blockPosition();

    private native void $script$write$blockPosition(BlockPos var1);

    private native BlockState $script$read$blockState();

    private native void $script$write$blockState(BlockState var1);
}

