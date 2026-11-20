package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;

public class EmptyRecord3 extends TelemetryRecord {
    public EmptyRecord3() {
    }

    public EmptyRecord3(TelemetryFlag flag, TelemetryCategory category) {
        super(flag, category);
    }

    @Override
    public void write(TelemetryOutput out) {
    }
}
