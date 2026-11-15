package com.adjustclient.util.anticheat.model;

import com.adjustclient.util.anticheat.blocker.BlockerState;
import net.minecraft.network.Packet;

public abstract class AntiCheatCheck {
    public byte id;
    public boolean passed;
    public void setServerData(BlockerState server) { }
    public abstract boolean onPacket(Packet<?> packet);
}
