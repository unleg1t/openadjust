package com.adjustclient.utilities.backend.protocol;

import com.adjustclient.utilities.backend.protocol.packet.ByteWriter;
import com.adjustclient.utilities.backend.protocol.packet.BytesReader;

final class HandshakeProtocolState {
    private HandshakeProtocolState() {
    }

    static void register(CerberusProtocolState state) {
        state.registerPacket(0, ByteWriter.class, ByteWriter::new);
        state.registerPacket(1, BytesReader.class, BytesReader::new);
    }
}
