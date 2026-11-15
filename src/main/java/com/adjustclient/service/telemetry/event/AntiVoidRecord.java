package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class AntiVoidRecord extends TelemetryRecord {
    public float fallDistance;
    public float horizontalDist;
    public float targetY;

    public AntiVoidRecord(TelemetryFlag flag, TelemetryCategory category, float fallDistance, float horizontalDist, float targetY) {
        super(flag, category);
        this.fallDistance = fallDistance;
        this.horizontalDist = horizontalDist;
        this.targetY = targetY;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeFloat(fallDistance);
        out.writeFloat(horizontalDist);
        out.writeFloat(targetY);
    }
}
