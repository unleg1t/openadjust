package com.adjustclient.utilities.anticheat.blocker;

import java.util.Objects;
import net.minecraft.client.Minecraft;

final class BlockedFeature {
    public WatchedPacketType feature;
    public PacketWatcher reason;
    public BlockedFeature(WatchedPacketType feature, PacketWatcher reason) {
        Objects.requireNonNull(Minecraft.getMinecraft().thePlayer);
        this.feature = feature;
        this.reason = reason;
    }
}
