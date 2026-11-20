package com.adjustclient.utilities.protocol.v1_12_2.login.server;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.login.LoginClientHandler;
import java.io.IOException;
import java.lang.String;
import java.security.PublicKey;

public class SPacketEncryptionRequest implements ProtocolPacket<LoginClientHandler> {
    private String serverId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    public SPacketEncryptionRequest() {
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
