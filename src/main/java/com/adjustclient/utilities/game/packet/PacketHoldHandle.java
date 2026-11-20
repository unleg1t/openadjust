package com.adjustclient.utilities.game.packet;

import com.adjustclient.utilities.Timer;

public final class PacketHoldHandle {
    public final HeldPacketQueue<?> queue;
    public PacketHold hold;
    public Timer timer;
    public boolean field16;
    public Object field20;

    public PacketHoldHandle(HeldPacketQueue<?> queue) { this.queue = queue; }

    public void acquire(boolean active) {
        if (hold != null) return;
        hold = queue.addHold(null, active);
        timer = new Timer();
    }

    public void release() {
        if (hold == null) return;
        queue.removeHold(hold);
        timer = null;
        hold = null;
    }
}
