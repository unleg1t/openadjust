package com.adjustclient.utilities.sunshine.check.packet;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import com.adjustclient.utilities.sunshine.CheckCategory;
import java.util.*;
public class NukerFrequencyCheck extends SunshineCheck implements OutgoingPacketListener {
    public final List<Timer> starts=new ArrayList<>();
    public Object field24;
    public NukerFrequencyCheck() { super("Nuker Frequency", "Housing Nuker digging frequency patch"); }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (!(packet instanceof C07PacketPlayerDigging) || ((C07PacketPlayerDigging)packet).getStatus()!=C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) return;
        starts.add(new Timer());
        starts.removeIf(timer -> timer.hasElapsed(50L));
        if (starts.size()>=11) fail(new ViolationDetails(CheckCategory.ALERT).put("count",starts.size())
                .put("first",System.currentTimeMillis()-starts.get(0).lastMs)
                .put("last",System.currentTimeMillis()-starts.get(starts.size()-1).lastMs));
    }
}
