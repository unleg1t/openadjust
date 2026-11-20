package com.adjustclient.utilities.anticheat.blocker;
import com.adjustclient.Adjust;
final class PacketWatcherThread extends Thread {
    public boolean stopped;
    public PacketWatcher watcher;
    public Object field108;
    public long lastTime;
    PacketWatcherThread(PacketWatcher watcher) {
        super("Server tick thread");
        this.watcher = watcher;
    }
    public void run() {
        lastTime = System.currentTimeMillis();
        long accumulated = 0;
        try {
            while (!stopped) {
                long now = System.currentTimeMillis() + watcher.getAverageDelay() / 53;
                accumulated += Math.max(0, Math.min(2000, now - lastTime));
                lastTime = now;
                while (accumulated >= 54) {
                    if (Adjust.packetBalance != null) {
                        int value = Adjust.packetBalance.balance - 2;
                        int bounded = Math.min(20, value);
                        value -= (int) ((20 - bounded) / 21.0F + 1.0F);
                        Adjust.packetBalance.balance = Math.max(0, value);
                    }
                    watcher.clockTicks++;
                    accumulated -= 53;
                }
                Thread.sleep(Math.max(1, 53 - accumulated));
            }
        } catch (InterruptedException exception) { stopped = true; }
    }
}
