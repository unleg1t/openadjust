package com.adjustclient.util.sunshine.check.raytrace;
import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.util.sunshine.*;
import com.adjustclient.util.sunshine.check.SunshineCheck;
import com.adjustclient.util.sunshine.listener.*;
import net.minecraft.network.Packet;
import com.adjustclient.util.sunshine.CheckCategory;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import com.adjustclient.module.impl.render.freecam.FreecamModule;
public class RaytraceCheck extends SunshineCheck implements RaytraceListener,OutgoingPacketListener {
    public RaytraceResult previous;
    public Object field24;
    public RaytraceCheck() { super("Raytrace","Detects tick raytrace mismatch"); }
    public void checkTarget(RaytraceResult next) {
        if (previous!=null && (previous.getClass()!=next.getClass() || !previous.matches(next))) {
            if (Adjust.gameStateTracker.state.game!=HypixelGame.HOUSING && (FreecamModule.INSTANCE.flags&2)==0)
                fail(new ViolationDetails(CheckCategory.ALERT).put("old",previous).put("new",next));
        }
        previous=next;
    }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement p=(C08PacketPlayerBlockPlacement)packet;
            if (p.getPlacedBlockDirection()!=255) checkTarget(new BlockRaytrace("PLACEMENT",p.getPosition(),
                    new Vec3(p.getPlacedBlockOffsetX(),p.getPlacedBlockOffsetY(),p.getPlacedBlockOffsetZ())));
        } else if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity p=(C02PacketUseEntity)packet;
            checkTarget(new EntityRaytrace(p.entityId,p.getHitVec()));
        } else if (packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging p=(C07PacketPlayerDigging)packet;
            java.util.Set<C07PacketPlayerDigging.Action> ignored=java.util.EnumSet.of(
                    C07PacketPlayerDigging.Action.DROP_ALL_ITEMS,C07PacketPlayerDigging.Action.DROP_ITEM,
                    C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,C07PacketPlayerDigging.Action.RELEASE_USE_ITEM);
            if (!ignored.contains(p.getStatus())) checkTarget(new BlockRaytrace(p.getStatus().name(),p.getPosition(),null));
        }
    }
}
