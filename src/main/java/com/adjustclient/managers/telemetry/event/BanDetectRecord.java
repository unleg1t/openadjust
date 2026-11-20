package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
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
