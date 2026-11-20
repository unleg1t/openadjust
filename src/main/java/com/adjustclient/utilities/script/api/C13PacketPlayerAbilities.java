package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class C13PacketPlayerAbilities
extends Packet {
    public boolean invulnerable;
    public boolean flying;
    public boolean allowFlying;
    public boolean creativeMode;
    public float flySpeed;
    public float walkSpeed;

    private native boolean $script$read$invulnerable();

    private native void $script$write$invulnerable(boolean var1);

    private native boolean $script$read$flying();

    private native void $script$write$flying(boolean var1);

    private native boolean $script$read$allowFlying();

    private native void $script$write$allowFlying(boolean var1);

    private native boolean $script$read$creativeMode();

    private native void $script$write$creativeMode(boolean var1);

    private native float $script$read$flySpeed();

    private native void $script$write$flySpeed(float var1);

    private native float $script$read$walkSpeed();

    private native void $script$write$walkSpeed(float var1);
}

