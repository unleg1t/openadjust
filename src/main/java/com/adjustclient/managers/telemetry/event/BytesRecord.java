package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import java.io.IOException;

public class BytesRecord extends TelemetryRecord {
    public Enum<?> type;
    public long value;

    public BytesRecord(TelemetryFlag flag, TelemetryCategory category, Enum<?> type, long value) {
        super(flag, category);
        this.type = type;
        this.value = value;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeByte(type.ordinal());
        out.writeVarLong(value);
    }
}
