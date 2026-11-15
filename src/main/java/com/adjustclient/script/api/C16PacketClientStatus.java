package com.adjustclient.script.api;

import com.adjustclient.script.api.ClientStatus;
import com.adjustclient.script.api.Packet;

public final class C16PacketClientStatus
extends Packet {
    public ClientStatus status;

    public C16PacketClientStatus() {
    }

    public C16PacketClientStatus(ClientStatus statusIn) {
    }

    private native ClientStatus $script$read$status();

    private native void $script$write$status(ClientStatus var1);
}

