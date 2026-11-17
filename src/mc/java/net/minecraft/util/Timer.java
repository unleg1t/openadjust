package net.minecraft.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Timer
{
    float ticksPerSecond;
    private double lastHRTime;
    public int elapsedTicks;
    public float renderPartialTicks;
    public float timerSpeed = 1.0F;
    public float elapsedPartialTicks;
    public float unscaledElapsedPartialTicks;
    public int unscaledElapsedTicks;
    private long lastSyncSysClock;
    private long lastSyncHRClock;
    private long counter;
    private double timeSyncAdjustment = 1.0D;

    public Timer(float tps)
    {
        this.ticksPerSecond = tps;
        this.lastSyncSysClock = Minecraft.getSystemTime();
        this.lastSyncHRClock = System.nanoTime() / 1000000L;
    }

    public void setTimerSpeed(float speed)
    {
        if (speed > 1.0F
                && com.adjustclient.util.anticheat.ServerTracker.INSTANCE != null
                && com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState != null
                && com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && com.adjustclient.util.anticheat.ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == com.adjustclient.server.ServerType.REWOUND
                && com.adjustclient.Adjust.rewoundDisablerTracker != null
                && !com.adjustclient.Adjust.rewoundDisablerTracker.canUseTimer())
        {
            return;
        }

        if (speed != 1.0F && this.timerSpeed == 1.0F)
        {
            this.unscaledElapsedPartialTicks = this.elapsedPartialTicks;
            this.unscaledElapsedTicks = this.elapsedTicks;
        }

        this.timerSpeed = speed;
    }

    public void updateTimer()
    {
        long i = Minecraft.getSystemTime();
        long j = i - this.lastSyncSysClock;
        long k = System.nanoTime() / 1000000L;
        double d0 = (double)k / 1000.0D;

        if (j <= 1000L && j >= 0L)
        {
            this.counter += j;

            if (this.counter > 1000L)
            {
                long l = k - this.lastSyncHRClock;
                double d1 = (double)this.counter / (double)l;
                this.timeSyncAdjustment += (d1 - this.timeSyncAdjustment) * 0.20000000298023224D;
                this.lastSyncHRClock = k;
                this.counter = 0L;
            }

            if (this.counter < 0L)
            {
                this.lastSyncHRClock = k;
            }
        }
        else
        {
            this.lastHRTime = d0;
        }

        this.lastSyncSysClock = i;
        double d2 = (d0 - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = d0;
        d2 = MathHelper.clamp_double(d2, 0.0D, 1.0D);
        this.elapsedPartialTicks = (float)((double)this.elapsedPartialTicks + d2 * (double)this.timerSpeed * (double)this.ticksPerSecond);
        this.elapsedTicks = (int)this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float)this.elapsedTicks;

        if (this.elapsedTicks > 10)
        {
            this.elapsedTicks = 10;
        }

        if (this.timerSpeed == 1.0F)
        {
            this.unscaledElapsedPartialTicks = this.elapsedPartialTicks;
            this.unscaledElapsedTicks = this.elapsedTicks;
        }
        else
        {
            this.unscaledElapsedPartialTicks = (float)((double)this.unscaledElapsedPartialTicks + d2 * (double)this.ticksPerSecond);
            this.unscaledElapsedTicks = (int)this.unscaledElapsedPartialTicks;
            this.unscaledElapsedPartialTicks -= (float)this.unscaledElapsedTicks;

            if (this.unscaledElapsedTicks > 10)
            {
                this.unscaledElapsedTicks = 10;
            }
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
    }
}
