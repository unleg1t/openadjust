package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.datasync.EntityDataManagerEntry;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;

public class SPacketEntityMetadata implements ProtocolPacket<PlayClientHandler> {
    private int entityId;
    private List<EntityDataManagerEntry<?>> dataEntries;

    public SPacketEntityMetadata() {
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
