package com.adjustclient.util.sunshine;
import net.minecraft.network.Packet;
import com.adjustclient.util.Timer;
public class TimedSample {
    public final Packet<?> value;
    public final Timer timer = new Timer();
    public Object field12;
    public TimedSample(Packet<?> value) { this.value = value; }
}
