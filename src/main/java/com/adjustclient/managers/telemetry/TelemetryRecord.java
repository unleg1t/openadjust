package com.adjustclient.managers.telemetry;

import java.io.IOException;

public abstract class TelemetryRecord {
    public TelemetryFlag flag;
    public TelemetryCategory category;
    public long timestamp;

    protected TelemetryRecord() {
    }

    protected TelemetryRecord(TelemetryFlag flag, TelemetryCategory category) {
        this.flag = flag;
        this.category = category;
    }

    public abstract void write(TelemetryOutput out) throws IOException;
}
