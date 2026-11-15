package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class PlayerNameRecord extends TelemetryRecord {
    public String name;
    public byte flags;

    public PlayerNameRecord(TelemetryFlag flag, TelemetryCategory category, String name, byte flags) {
        super(flag, category);
        this.name = name;
        this.flags = flags;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeUTF(name);
        out.writeByte(flags);
    }
}
