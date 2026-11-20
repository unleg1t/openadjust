package com.adjustclient.managers.telemetry.event;

import com.adjustclient.managers.telemetry.TelemetryCategory;
import com.adjustclient.managers.telemetry.TelemetryFlag;
import com.adjustclient.managers.telemetry.TelemetryOutput;
import com.adjustclient.managers.telemetry.TelemetryRecord;
import java.io.IOException;

public class ServerAddressRecord extends TelemetryRecord {
    public String address;

    public ServerAddressRecord(TelemetryFlag flag, TelemetryCategory category, String address) {
        super(flag, category);
        this.address = address;
    }

    @Override
    public void write(TelemetryOutput out) throws IOException {
        out.writeBoolean(address == null);
        if (address != null) {
            out.writeUTF(address);
        }
    }
}
