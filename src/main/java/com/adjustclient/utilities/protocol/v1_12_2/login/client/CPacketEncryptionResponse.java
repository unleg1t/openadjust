package com.adjustclient.utilities.protocol.v1_12_2.login.client;

import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacket;
import com.adjustclient.utilities.protocol.v1_12_2.ProtocolPacketBuffer;
import com.adjustclient.utilities.protocol.v1_12_2.login.LoginServerHandler;
import java.io.IOException;

public class CPacketEncryptionResponse implements ProtocolPacket<LoginServerHandler> {
    private byte[] secretKeyEncrypted;
    private byte[] verifyTokenEncrypted;

    public CPacketEncryptionResponse() {
        this.secretKeyEncrypted = new byte[0];
        this.verifyTokenEncrypted = new byte[0];
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
