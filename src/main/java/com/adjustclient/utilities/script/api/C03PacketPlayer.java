package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public class C03PacketPlayer
extends Packet {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean onGround;
    public boolean moving;
    public boolean rotating;
    public boolean teleportResponse;

    public C03PacketPlayer() {
    }

    public C03PacketPlayer(boolean isOnGround) {
    }

    private native double $script$read$x();

    private native void $script$write$x(double var1);

    private native double $script$read$y();

    private native void $script$write$y(double var1);

    private native double $script$read$z();

    private native void $script$write$z(double var1);

    private native float $script$read$yaw();

    private native void $script$write$yaw(float var1);

    private native float $script$read$pitch();

    private native void $script$write$pitch(float var1);

    private native boolean $script$read$onGround();

    private native void $script$write$onGround(boolean var1);

    private native boolean $script$read$moving();

    private native void $script$write$moving(boolean var1);

    private native boolean $script$read$rotating();

    private native void $script$write$rotating(boolean var1);

    private native boolean $script$read$teleportResponse();

    private native void $script$write$teleportResponse(boolean var1);
}

