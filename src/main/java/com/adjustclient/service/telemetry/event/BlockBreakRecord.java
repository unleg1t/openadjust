package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public class BlockBreakRecord extends TelemetryRecord {
    public BlockPos pos;
    public IBlockState state;
    public short elapsedMs;

    public BlockBreakRecord(TelemetryFlag flag, TelemetryCategory category, BlockPos pos, IBlockState state, short elapsedMs) {
        super(flag, category);
        this.pos = pos;
        this.state = state;
        this.elapsedMs = elapsedMs;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeLong(pos.toLong());
        out.writeVarInt(Block.getStateId(state));
        out.writeShort(elapsedMs);
    }
}
