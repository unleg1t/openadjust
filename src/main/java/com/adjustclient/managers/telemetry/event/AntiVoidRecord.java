package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
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
