package com.adjustclient.script.api;

import com.adjustclient.script.api.EnumFlags;
import com.adjustclient.script.api.Packet;
import java.util.Set;

public final class S08PacketPlayerPosLook
extends Packet {
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public Set<EnumFlags> flags;

    public S08PacketPlayerPosLook() {
    }

    public S08PacketPlayerPosLook(double xIn, double yIn, double zIn, float yawIn, float pitchIn, Set<EnumFlags> p_i45993_9_) {
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

    private native Set<EnumFlags> $script$read$flags();

    private native void $script$write$flags(Set<EnumFlags> var1);
}

