package com.adjustclient.script.api;

import com.adjustclient.script.api.Entity;
import com.adjustclient.script.api.Packet;

public final class S12PacketEntityVelocity
extends Packet {
    public int entityID;
    public int motionX;
    public int motionY;
    public int motionZ;

    public S12PacketEntityVelocity() {
    }

    public S12PacketEntityVelocity(Entity entityIn) {
    }

    public S12PacketEntityVelocity(int entityIDIn, double motionXIn, double motionYIn, double motionZIn) {
    }

    public S12PacketEntityVelocity(int entityID, int motionX, int motionY, int motionZ) {
    }

    private native int $script$read$entityID();

    private native void $script$write$entityID(int var1);

    private native int $script$read$motionX();

    private native void $script$write$motionX(int var1);

    private native int $script$read$motionY();

    private native void $script$write$motionY(int var1);

    private native int $script$read$motionZ();

    private native void $script$write$motionZ(int var1);
}

