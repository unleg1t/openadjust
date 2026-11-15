package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;

public class EmptyRecord extends TelemetryRecord {
    public EmptyRecord() {
    }

    public EmptyRecord(TelemetryFlag flag, TelemetryCategory category) {
        super(flag, category);
    }

    @Override
    public void write(TelemetryOutput out) {
    }
}
