package com.adjustclient.protocol.v1_12_2.play.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.play.PlayClientHandler;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.play.server.S20PacketEntityProperties.Snapshot;

public class SPacketEntityProperties implements ProtocolPacket<PlayClientHandler> {
    private int entityId;
    private final List<Snapshot> snapshots;

    public SPacketEntityProperties() {
        this.snapshots = new java.util.ArrayList<>();
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
