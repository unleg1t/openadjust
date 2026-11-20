package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
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
