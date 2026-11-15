package com.adjustclient.game.packet;

import net.minecraft.network.Packet;

public final class QueuedPacket {
    public final Packet<?> packet;
    public final Runnable sendAction;
    public final Object sendArgument;
    public final Object sendArguments;
    public Object field20;
    public final long sequence;
    public final long enqueueTime;

    public QueuedPacket(Packet<?> packet, Runnable sendAction, Object sendArgument,
                        Object sendArguments, long sequence, long enqueueTime) {
        this.packet = packet;
        this.sendAction = sendAction;
        this.sendArgument = sendArgument;
        this.sendArguments = sendArguments;
        this.sequence = sequence;
        this.enqueueTime = enqueueTime;
    }
}
