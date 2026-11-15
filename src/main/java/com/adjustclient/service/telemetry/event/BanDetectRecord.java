package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class BanDetectRecord extends TelemetryRecord {
    public String playerName;

    public BanDetectRecord(TelemetryFlag flag, TelemetryCategory category, String playerName) {
        super(flag, category);
        this.playerName = playerName;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeUTF(playerName);
    }
}
