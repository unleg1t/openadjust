package com.adjustclient.utilities.protocol.v1_12_2.login.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.login.LoginClientHandler;
import com.mojang.authlib.GameProfile;
import java.io.IOException;

public class SPacketLoginSuccess implements ProtocolPacket<LoginClientHandler> {
    private GameProfile profile;

    public SPacketLoginSuccess() {
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
