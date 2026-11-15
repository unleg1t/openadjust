package com.adjustclient.script.api;

import com.adjustclient.script.api.Entity;
import com.adjustclient.script.api.Packet;

public final class S0BPacketAnimation
extends Packet {
    public int entityId;
    public int type;

    public S0BPacketAnimation() {
    }

    public S0BPacketAnimation(Entity ent, int animationType) {
    }

    public S0BPacketAnimation(int entityId, int type) {
    }

    private native int $script$read$entityId();

    private native void $script$write$entityId(int var1);

    private native int $script$read$type();

    private native void $script$write$type(int var1);
}

