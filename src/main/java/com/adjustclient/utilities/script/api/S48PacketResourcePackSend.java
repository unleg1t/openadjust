package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class S48PacketResourcePackSend
extends Packet {
    public String url;
    public String hash;

    public S48PacketResourcePackSend() {
    }

    public S48PacketResourcePackSend(String url, String hash) {
    }

    private native String $script$read$url();

    private native void $script$write$url(String var1);

    private native String $script$read$hash();

    private native void $script$write$hash(String var1);
}

