package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.BlockPos;
import com.adjustclient.utilities.script.api.Packet;
import com.adjustclient.utilities.script.api.Vec3;
import java.util.List;

public final class S27PacketExplosion
extends Packet {
    public double posX;
    public double posY;
    public double posZ;
    public float strength;
    public List<BlockPos> affectedBlockPositions;
    public float motionX;
    public float motionY;
    public float motionZ;
    public boolean blockMotion;

    public S27PacketExplosion() {
    }

    public S27PacketExplosion(double p_i45193_1_, double y, double z, float strengthIn, List<BlockPos> affectedBlocksIn, Vec3 motion) {
    }

    public S27PacketExplosion(double posX, double posY, double posZ, float strength, List<BlockPos> affectedBlockPositions, float motionX, float motionY, float motionZ) {
    }

    private native double $script$read$posX();

    private native void $script$write$posX(double var1);

    private native double $script$read$posY();

    private native void $script$write$posY(double var1);

    private native double $script$read$posZ();

    private native void $script$write$posZ(double var1);

    private native float $script$read$strength();

    private native void $script$write$strength(float var1);

    private native List<BlockPos> $script$read$affectedBlockPositions();

    private native void $script$write$affectedBlockPositions(List<BlockPos> var1);

    private native float $script$read$motionX();

    private native void $script$write$motionX(float var1);

    private native float $script$read$motionY();

    private native void $script$write$motionY(float var1);

    private native float $script$read$motionZ();

    private native void $script$write$motionZ(float var1);

    private native boolean $script$read$blockMotion();

    private native void $script$write$blockMotion(boolean var1);
}

