package com.adjustclient.backend.protocol.packet;

import com.google.common.base.Charsets;
import io.netty.handler.codec.EncoderException;

public class StringPacket extends CerberusPacket<String[]> {
    public String input;
    public String[] result;

    public StringPacket(String input) {
        super((byte) 9);
        this.input = input;
    }

    @Override
    public void write(CerberusPacketData data) {
        byte[] bytes = input.getBytes(Charsets.UTF_8);
        if (bytes.length > 32767) {
            throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max 32767)");
        }
        data.buffer.writeVarIntToBuffer(bytes.length);
        data.buffer.writeBytes(bytes);
    }

    @Override
    public void read(CerberusPacketData data) {
        String first = data.buffer.readStringFromBuffer(32000);
        String second = data.buffer.readStringFromBuffer(32000);
        result = new String[] { first, second };
    }

    @Override
    public String[] getResult() {
        return result;
    }
}
