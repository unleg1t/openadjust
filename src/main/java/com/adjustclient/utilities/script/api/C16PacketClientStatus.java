package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ClientStatus;
import com.adjustclient.utilities.script.api.Packet;

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

