package com.adjustclient.service.telemetry;

public class TelemetryBatch {
    public final TelemetryRecord[] records;
    public final byte[] data;
    public final long timestamp;

    public TelemetryBatch(TelemetryRecord[] records, byte[] data, long timestamp) {
        this.records = records;
        this.data = data;
        this.timestamp = timestamp;
    }
}
