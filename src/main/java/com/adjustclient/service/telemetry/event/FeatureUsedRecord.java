package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.BypassFeature;
import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class FeatureUsedRecord extends TelemetryRecord {
    public BypassFeature feature;

    public FeatureUsedRecord(TelemetryFlag flag, TelemetryCategory category, BypassFeature feature) {
        super(flag, category);
        this.feature = feature;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeVarInt(feature.ordinal());
    }
}
