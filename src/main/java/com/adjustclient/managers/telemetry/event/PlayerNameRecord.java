package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
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
