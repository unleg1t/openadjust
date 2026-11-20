package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.BypassFeature;
import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
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
