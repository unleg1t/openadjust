package com.adjustclient.service.telemetry.event;

import com.adjustclient.service.telemetry.TelemetryCategory;
import com.adjustclient.service.telemetry.TelemetryFlag;
import com.adjustclient.service.telemetry.TelemetryOutput;
import com.adjustclient.service.telemetry.TelemetryRecord;
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
