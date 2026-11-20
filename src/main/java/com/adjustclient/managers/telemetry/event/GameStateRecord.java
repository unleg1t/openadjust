package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import java.io.IOException;

public class GameStateRecord extends TelemetryRecord {
    public Enum<?> game;
    public byte stateFlag;

    public GameStateRecord(TelemetryFlag flag, TelemetryCategory category, Enum<?> game, byte stateFlag) {
        super(flag, category);
        this.game = game;
        this.stateFlag = stateFlag;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeByte(game.ordinal());
        out.writeByte(stateFlag);
    }
}
