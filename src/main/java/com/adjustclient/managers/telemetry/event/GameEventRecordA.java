package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import java.io.IOException;

public class GameEventRecordA extends TelemetryRecord {
    public String data;

    public GameEventRecordA(TelemetryFlag flag, TelemetryCategory category, String data) {
        super(flag, category);
        this.data = data;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeUTF(data);
    }
}
