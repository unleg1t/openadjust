package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;
import net.minecraft.util.BlockPos;

public class FloatPairRecord extends TelemetryRecord {
    public BlockPos pos;
    public short deltaMs;

    public FloatPairRecord(TelemetryFlag flag, TelemetryCategory category, BlockPos pos, short deltaMs) {
        super(flag, category);
        this.pos = pos;
        this.deltaMs = deltaMs;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeLong(pos.toLong());
        out.writeShort(deltaMs);
    }
}
