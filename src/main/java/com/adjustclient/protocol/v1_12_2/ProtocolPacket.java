package com.adjustclient.protocol.v1_12_2;

public interface ProtocolPacket<T extends ProtocolNetHandler> {
    void readPacketData(ProtocolPacketBuffer buffer) throws java.io.IOException;
    void writePacketData(ProtocolPacketBuffer buffer) throws java.io.IOException;
    void processPacket(T handler);
}
