package com.adjustclient.utilities.game.packet;

import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public abstract class HeldPacketQueue<T extends INetHandler> {
    public final ArrayList<PacketHold> holds = new ArrayList<>();
    public final ArrayList<QueuedPacket> packets = new ArrayList<>();
    public final Object lock = new Object();
    public PacketTransform transform;
    public Object field20;
    public long sequence;

    public boolean isPacketAllowed(Packet<?> packet) {
        PacketQueueFilter filter = holds.getFirst().filter;
        return filter == null || filter.isPacketQueueAllowed(packet);
    }

    public void offer(Packet<?> packet, Runnable sendAction, Object sendArgument, Object sendArguments) {
        synchronized (lock) {
            if (!holds.isEmpty() && isPacketAllowed(packet)) {
                packets.add(new QueuedPacket(packet, sendAction, sendArgument, sendArguments,
                        sequence, System.currentTimeMillis()));
                sequence++;
                return;
            }
        }
        sendAction.run();
    }

    public void clear() {
        synchronized (lock) {
            holds.clear();
            packets.clear();
            sequence = 0;
        }
    }

    public void removeHold(PacketHold hold) {
        synchronized (lock) {
            if (!holds.contains(hold)) return;
            holds.remove(hold);
            sortHolds();
            flushUntil(holds.isEmpty() ? null : holds.getFirst());
        }
    }

    public PacketHold addHold(PacketQueueFilter filter, boolean active) {
        synchronized (lock) {
            PacketHold hold = new PacketHold(filter, updateOldestHoldTime(), active);
            holds.add(hold);
            return hold;
        }
    }

    public void flushUntil(PacketHold hold) {
        ArrayList<QueuedPacket> ready = new ArrayList<>();
        for (QueuedPacket packet : packets) {
            if (hold == null || packet.sequence < hold.sequence) ready.add(packet);
        }
        for (QueuedPacket packet : ready) {
            packet.sendAction.run();
            packets.remove(packet);
        }
    }

    public long updateOldestHoldTime() {
        long oldest = sequence;
        for (PacketHold hold : holds) {
            if (hold.active) oldest = Math.min(oldest, hold.sequence);
        }
        return oldest;
    }

    public void sortHolds() {
        holds.sort(Comparator.comparingLong(
                hold -> hold.sequence));
        packets.sort(Comparator.comparingLong(
                packet -> packet.sequence));
    }
}
