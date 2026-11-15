package com.adjustclient.game.packet;

import java.util.ArrayList;

public final class PacketBatch {
    public final HeldPacketQueue<?> queue;
    public Object field8;
    public final ArrayList<PacketHold> holds = new ArrayList<>();
    public Object field16;

    public PacketBatch(HeldPacketQueue<?> queue) { this.queue = queue; }

    public void addHold() {
        synchronized (holds) {
            holds.add(queue.addHold(null, false));
        }
    }
}
