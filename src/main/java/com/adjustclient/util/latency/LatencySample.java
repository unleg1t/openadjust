package com.adjustclient.util.latency;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
public class LatencySample {
    public boolean waiting;
    public long movementTime;
    public long adjustedTime;
    public long margin;
    public Packet<?> packet;
    public LatencySample(long adjustedTime, long margin) { this.adjustedTime = adjustedTime; this.margin = margin; }
    public void onPacket(TransactionQueue queue, Packet<?> packet) {
        if (waiting && packet instanceof C0FPacketConfirmTransaction) {
            var state = com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState;
            if (state != null) {
                var watcher = state.packetWatcher;
                adjustedTime = queue.lastTime - 1000000L * watcher.getAverageDelay();
                waiting = false;
            }
        }
        if (packet instanceof C03PacketPlayer) {
            waiting = true;
            movementTime += 50000000L;
            if (movementTime > System.nanoTime()) movementTime -= 50000000L;
            movementTime = Math.max(adjustedTime - margin, movementTime);
        }
    }
}
