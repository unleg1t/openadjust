package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class S3APacketTabComplete
extends Packet {
    public String[] matches;

    public S3APacketTabComplete() {
    }

    public S3APacketTabComplete(String[] matchesIn) {
    }

    private native String[] $script$read$matches();

    private native void $script$write$matches(String[] var1);
}

