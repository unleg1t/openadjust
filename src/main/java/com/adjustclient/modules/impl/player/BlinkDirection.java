package com.adjustclient.modules.impl.player;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.game.packet.HeldPacketQueue;
import com.adjustclient.core.settings.ModeOption;

public enum BlinkDirection implements ModeOption {
    OUTBOUND("Outbound", Adjust.outboundPacketQueue),
    INBOUND("Inbound", Adjust.inboundPacketQueue),
    BOTH("Both", Adjust.inboundPacketQueue, Adjust.outboundPacketQueue);

    private final String displayName;
    public final HeldPacketQueue<?>[] queues;
    BlinkDirection(String displayName, HeldPacketQueue<?>... queues) {
        this.displayName = displayName;
        this.queues = queues;
    }
    @Override public String getName() { return displayName; }
}
