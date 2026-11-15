package com.adjustclient.backend.protocol.netty;

import com.adjustclient.backend.protocol.CerberusProtocolState;

public final class CerberusPackets {
    public static final int[] PROTOCOL_STATES = new int[CerberusProtocolState.values().length];

    static {
        PROTOCOL_STATES[CerberusProtocolState.HANDSHAKE.ordinal()] = 1;
        PROTOCOL_STATES[CerberusProtocolState.SESSION.ordinal()] = 2;
    }

    private CerberusPackets() {
    }
}
