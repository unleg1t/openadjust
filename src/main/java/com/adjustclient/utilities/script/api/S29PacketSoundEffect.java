package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Packet;

public final class S29PacketSoundEffect
extends Packet {
    public String soundName;
    public int posX;
    public int posY;
    public int posZ;
    public float soundVolume;
    public int soundPitch;

    public S29PacketSoundEffect() {
    }

    public S29PacketSoundEffect(String soundNameIn, double soundX, double soundY, double soundZ, float volume, float pitch) {
    }

    private native String $script$read$soundName();

    private native void $script$write$soundName(String var1);

    private native int $script$read$posX();

    private native void $script$write$posX(int var1);

    private native int $script$read$posY();

    private native void $script$write$posY(int var1);

    private native int $script$read$posZ();

    private native void $script$write$posZ(int var1);

    private native float $script$read$soundVolume();

    private native void $script$write$soundVolume(float var1);

    private native int $script$read$soundPitch();

    private native void $script$write$soundPitch(int var1);
}

