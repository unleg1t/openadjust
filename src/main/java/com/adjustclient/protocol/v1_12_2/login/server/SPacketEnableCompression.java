package com.adjustclient.protocol.v1_12_2.login.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.login.LoginClientHandler;
import java.io.IOException;

public class SPacketEnableCompression implements ProtocolPacket<LoginClientHandler> {
    private int compressionThreshold;

    public SPacketEnableCompression() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(LoginClientHandler handler) {
    }
}
