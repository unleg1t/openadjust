package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
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
