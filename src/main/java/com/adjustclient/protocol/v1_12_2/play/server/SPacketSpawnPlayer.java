package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.datasync.EntityDataManagerEntry;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class SPacketSpawnPlayer implements ProtocolPacket<PlayClientHandler> {
    private int entityId;
    private UUID uniqueId;
    private double x;
    private double y;
    private double z;
    private byte yaw;
    private byte pitch;
    private List<EntityDataManagerEntry<?>> dataEntries;

    public SPacketSpawnPlayer() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayClientHandler handler) {
    }
}
