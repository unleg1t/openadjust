package com.adjustclient.util.latency;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import org.apache.commons.lang3.tuple.Pair;
public final class LatencyTracker {
    public TransactionQueue transactions;
    public LatencyState state = new LatencyState();
    public LatencySample sample;
    public Object field16;
    public LatencyTracker() { reset(); }
    public void reset() {
        long now = System.nanoTime();
        System.currentTimeMillis();
        transactions = new TransactionQueue(now);
        sample = new LatencySample(System.nanoTime() - 60000000000L, 120L);
    }
    public void onReceive(Packet<?> packet) {
        if (transactions == null) return;
        if (packet instanceof S00PacketKeepAlive) {
            java.util.Objects.requireNonNull(state);
            int id = ((S00PacketKeepAlive) packet).getId();
            if (id < 0 && id == (short) id) {
                transactions.pending.add(Pair.of((short) id, System.nanoTime()));
                transactions.received.incrementAndGet();
            }
        }
    }
}
