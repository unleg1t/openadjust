package com.adjustclient.script.api;

import com.adjustclient.script.api.Packet;

public final class S06PacketUpdateHealth
extends Packet {
    public float health;
    public int foodLevel;
    public float saturationLevel;

    public S06PacketUpdateHealth() {
    }

    public S06PacketUpdateHealth(float healthIn, int foodLevelIn, float saturationIn) {
    }

    private native float $script$read$health();

    private native void $script$write$health(float var1);

    private native int $script$read$foodLevel();

    private native void $script$write$foodLevel(int var1);

    private native float $script$read$saturationLevel();

    private native void $script$write$saturationLevel(float var1);
}

