package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
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
