package com.adjustclient.utilities.protocol.v1_12_2.play.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;
import net.minecraft.network.play.client.C19PacketResourcePackStatus.Action;

public class CPacketResourcePackStatus implements ProtocolPacket<PlayServerHandler> {
    private Action action;

    public CPacketResourcePackStatus() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(PlayServerHandler handler) {
    }
}
