package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class GameStartRecord extends TelemetryRecord {
    public Enum<?> game;

    public GameStartRecord(TelemetryFlag flag, TelemetryCategory category, Enum<?> game) {
        super(flag, category);
        this.game = game;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeByte(game.ordinal());
    }
}
