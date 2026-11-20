package com.adjustclient.managers.telemetry;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TelemetryWriter {
    public final UUID uuid;
    public final String username;
    public final List<TelemetryRecord> records = new ArrayList<>();
    public final Set<BypassFeature> usedFeatures = new HashSet<>();
    public int recordCount;

    public TelemetryWriter(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public void add(TelemetryRecord record) {
        add(record, System.currentTimeMillis());
    }

    public void add(TelemetryRecord record, long time) {
        if (record == null) {
            return;
        }
        record.timestamp = time;
        synchronized (records) {
            records.add(record);
        }
        recordCount++;
    }

    public TelemetryBatch buildBatch() {
        TelemetryRecord[] snapshot;
        synchronized (records) {
            snapshot = records.toArray(new TelemetryRecord[0]);
            records.clear();
        }
        long now = System.currentTimeMillis();
        ByteArrayOutputStream outerStream = new ByteArrayOutputStream();
        TelemetryOutput outer = new TelemetryOutput(outerStream);
        TelemetryOutput payloadOut = new TelemetryOutput();
        for (TelemetryRecord record : snapshot) {
            Integer typeId = TelemetryConstants.RECORD_IDS.get(record.getClass());
            if (typeId == null) {
                throw new IllegalStateException("Unknown event type: " + record.getClass());
            }
            try {
                outer.writeByte(typeId);
                outer.writeVarLong(now - record.timestamp);
                payloadOut.reset();
                record.write(payloadOut);
                byte[] payload = payloadOut.toByteArray();
                outer.writeVarInt(payload.length);
                outer.writeBytes(payload);
            } catch (IOException e) {
                Logger.log(LogLevel.ERROR, true, "IO in flush: {}", e);
            }
        }
        return new TelemetryBatch(snapshot, outerStream.toByteArray(), now);
    }
}
