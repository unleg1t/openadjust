package com.adjustclient.script.api;

import com.adjustclient.script.api.Packet;

public class S14PacketEntity
extends Packet {
    public int entityId;
    public byte posX;
    public byte posY;
    public byte posZ;
    public byte yaw;
    public byte pitch;
    public boolean onGround;
    public boolean rotating;

    public S14PacketEntity() {
    }

    public S14PacketEntity(int entityIdIn) {
    }

    private native int $script$read$entityId();

    private native void $script$write$entityId(int var1);

    private native byte $script$read$posX();

    private native void $script$write$posX(byte var1);

    private native byte $script$read$posY();

    private native void $script$write$posY(byte var1);

    private native byte $script$read$posZ();

    private native void $script$write$posZ(byte var1);

    private native byte $script$read$yaw();

    private native void $script$write$yaw(byte var1);

    private native byte $script$read$pitch();

    private native void $script$write$pitch(byte var1);

    private native boolean $script$read$onGround();

    private native void $script$write$onGround(boolean var1);

    private native boolean $script$read$rotating();

    private native void $script$write$rotating(boolean var1);
}

