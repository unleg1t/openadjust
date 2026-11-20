package com.adjustclient.utilities.protocol.v1_12_2.login.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.login.LoginServerHandler;
import com.mojang.authlib.GameProfile;
import java.io.IOException;

public class CPacketLoginStart implements ProtocolPacket<LoginServerHandler> {
    private GameProfile profile;

    public CPacketLoginStart() {
    }

    @Override
    public void readPacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void writePacketData(ProtocolPacketBuffer buffer) throws IOException {
    }

    @Override
    public void processPacket(LoginServerHandler handler) {
    }
}
