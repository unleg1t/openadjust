package com.adjustclient.utilities.sunshine.check.player;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
import com.adjustclient.utilities.sunshine.CheckCategory;
import net.minecraft.network.play.client.*;
public final class EntityActionCheck extends SunshineCheck implements OutgoingPacketListener {
    public boolean sprint;
    public boolean sneak;
    public Object field24;
    public EntityActionCheck() { super("Entity Action", "Duplicate C0BPacketEntityAction"); }
    public void reportAction(C0BPacketEntityAction.Action action) {
        fail(new ViolationDetails(CheckCategory.KICK).put("action",action).put("sneak",sneak).put("sprint",sprint));
    }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer) { sneak=false; sprint=false; return; }
        if (!(packet instanceof C0BPacketEntityAction)) return;
        C0BPacketEntityAction.Action action=((C0BPacketEntityAction)packet).getAction();
        switch (action) {
            case START_SNEAKING: case STOP_SNEAKING:
                if (sneak) reportAction(action);
                sneak=true;
                break;
            case START_SPRINTING: case STOP_SPRINTING:
                java.util.Objects.requireNonNull(anticheat.positionTracker);
                if (sprint) reportAction(action);
                sprint=true;
                break;
            default: break;
        }
    }
}
