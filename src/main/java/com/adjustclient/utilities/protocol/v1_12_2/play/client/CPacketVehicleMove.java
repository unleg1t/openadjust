package com.adjustclient.utilities.protocol.v1_12_2.play.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.play.PlayServerHandler;
import java.io.IOException;

public class CPacketVehicleMove implements ProtocolPacket<PlayServerHandler> {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public CPacketVehicleMove() {
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
