package com.adjustclient.protocol.v1_12_2.handshake.client;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.ProtocolState;
import com.adjustclient.protocol.v1_12_2.handshake.HandshakeServerHandler;
import java.io.IOException;
import java.lang.String;

public class CPacketHandshake implements ProtocolPacket<HandshakeServerHandler> {
    private int protocolVersion;
    private String ip;
    private int port;
    private ProtocolState requestedState;

    public CPacketHandshake() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(HandshakeServerHandler handler) {
    }
}
