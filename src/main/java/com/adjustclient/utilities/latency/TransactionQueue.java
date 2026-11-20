package com.adjustclient.utilities.latency;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.tuple.Pair;
public final class TransactionQueue {
    public ConcurrentLinkedQueue<Pair<Short, Long>> pending = new ConcurrentLinkedQueue<>();
    public AtomicInteger received = new AtomicInteger();
    public AtomicInteger field12 = new AtomicInteger();
    public AtomicInteger acknowledged = new AtomicInteger();
    public Object field20;
    public long lastTime;
    public TransactionQueue(long now) { lastTime = now; }
    public boolean acknowledge(short id) {
        int skipped = 0;
        for (Pair<Short, Long> item : pending) {
            if (item.getLeft() != id) { skipped++; continue; }
            if (skipped >= 1) System.currentTimeMillis();
            Pair<Short, Long> taken;
            while ((taken = pending.poll()) != null) {
                acknowledged.incrementAndGet();
                System.currentTimeMillis();
                System.nanoTime();
                lastTime = taken.getRight();
                if (taken.getLeft() == id) return true;
            }
            return false;
        }
        return false;
    }
}
