package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class C01PacketChatMessage
extends Packet {
    public String message;

    private native String $script$read$message();

    private native void $script$write$message(String var1);
}

