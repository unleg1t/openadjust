package com.adjustclient.managers.servertracker;

import com.adjustclient.utilities.ByteUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class KnownPlayers {
    public final Map<String, TrackerScope> players = new LinkedHashMap<>();
    public long timestamp;
    public Object field16;

    public void readSnapshot(DataInputStream input) throws IOException {
        timestamp = ByteUtil.readVarLong(input) + 1728500000000L;
        players.clear();
        int count = input.readUnsignedByte();
        for (int i = 0; i < count; i++) {
            String name = input.readUTF();
            TrackerScope scope = TrackerScope.values()[input.readUnsignedByte()];
            players.put(name, scope);
        }
    }
}
