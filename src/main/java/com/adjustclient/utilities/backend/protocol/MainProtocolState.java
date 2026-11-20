package com.adjustclient.utilities.backend.protocol;

import com.adjustclient.utilities.backend.protocol.packet.BytesCodec;
import com.adjustclient.utilities.backend.protocol.packet.BytesWriter;
import com.adjustclient.utilities.backend.protocol.packet.EmptyReader;
import com.adjustclient.utilities.backend.protocol.packet.EmptyWriter;

final class MainProtocolState {
    private MainProtocolState() {
    }

    static void register(CerberusProtocolState state) {
        state.registerPacket(0, BytesWriter.class, BytesWriter::new);
        state.registerPacket(1, BytesCodec.class, BytesCodec::new);
        state.registerPacket(2, EmptyWriter.class, EmptyWriter::new);
        state.registerPacket(3, EmptyReader.class, EmptyReader::new);
    }
}
