package com.adjustclient.protocol.v1_12_2.login.server;

import com.adjustclient.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.protocol.v1_12_2.login.LoginClientHandler;
import java.io.IOException;
import net.minecraft.util.IChatComponent;

public class SPacketLoginDisconnect implements ProtocolPacket<LoginClientHandler> {
    private IChatComponent reason;

    public SPacketLoginDisconnect() {
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
