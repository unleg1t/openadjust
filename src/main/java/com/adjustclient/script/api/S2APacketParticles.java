package com.adjustclient.script.api;

import com.adjustclient.script.api.EnumParticleTypes;
import com.adjustclient.script.api.Packet;

public final class S2APacketParticles
extends Packet {
    public EnumParticleTypes particleType;
    public float xCoord;
    public float yCoord;
    public float zCoord;
    public float xOffset;
    public float yOffset;
    public float zOffset;
    public float particleSpeed;
    public int particleCount;
    public boolean longDistance;
    public int[] particleArguments;

    private native EnumParticleTypes $script$read$particleType();

    private native void $script$write$particleType(EnumParticleTypes var1);

    private native float $script$read$xCoord();

    private native void $script$write$xCoord(float var1);

    private native float $script$read$yCoord();

    private native void $script$write$yCoord(float var1);

    private native float $script$read$zCoord();

    private native void $script$write$zCoord(float var1);

    private native float $script$read$xOffset();

    private native void $script$write$xOffset(float var1);

    private native float $script$read$yOffset();

    private native void $script$write$yOffset(float var1);

    private native float $script$read$zOffset();

    private native void $script$write$zOffset(float var1);

    private native float $script$read$particleSpeed();

    private native void $script$write$particleSpeed(float var1);

    private native int $script$read$particleCount();

    private native void $script$write$particleCount(int var1);

    private native boolean $script$read$longDistance();

    private native void $script$write$longDistance(boolean var1);

    private native int[] $script$read$particleArguments();

    private native void $script$write$particleArguments(int[] var1);
}

