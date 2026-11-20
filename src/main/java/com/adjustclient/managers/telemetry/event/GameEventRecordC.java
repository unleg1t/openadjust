package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import java.io.IOException;

public class GameEventRecordC extends TelemetryRecord {
    public String name;

    public GameEventRecordC(TelemetryFlag flag, TelemetryCategory category, String name) {
        super(flag, category);
        this.name = name;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeUTF(name);
    }
}
