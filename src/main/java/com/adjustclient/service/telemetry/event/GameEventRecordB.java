package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
import java.io.IOException;

public class GameEventRecordB extends TelemetryRecord {
    public String team;
    public String players;

    public GameEventRecordB(TelemetryFlag flag, TelemetryCategory category, String team, String players) {
        super(flag, category);
        this.team = team;
        this.players = players;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeUTF(team);
        out.writeUTF(players);
    }
}
