package com.adjustclient.utilities.sunshine;
import net.minecraft.network.Packet;
import com.adjustclient.utilities.Timer;
public class TimedSample {
    public final Packet<?> value;
    public final Timer timer = new Timer();
    public Object field12;
    public TimedSample(Packet<?> value) { this.value = value; }
}
