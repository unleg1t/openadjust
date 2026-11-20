package com.adjustclient.utilities.backend.protocol.packet;

public class PingPacket extends CerberusPacket<Byte> {
    public byte value;
    public byte response;

    public PingPacket(byte value) {
        super((byte) 10);
        this.value = value;
    }

    @Override
    public void write(CerberusPacketData data) {
        data.buffer.writeByte(value);
    }

    @Override
    public void read(CerberusPacketData data) {
        response = data.buffer.readByte();
    }

    @Override
    public Byte getResult() {
        return response;
    }
}
