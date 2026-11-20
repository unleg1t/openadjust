package com.adjustclient.utilities.placement;
import java.util.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
public final class PlacementVerifier {
    public PlacementSnapshot snapshot = new PlacementSnapshot();
    public List<PlacementCheck<?>> checks = new ArrayList<>();
    public PlacementFacingTracker facingTracker = new PlacementFacingTracker(this);
    public List<SimplePlacementContext> contexts = new ArrayList<>();
    public SimplePlacementContext context;
    public Object field24;
    public PlacementVerifier() {
        context = new SimplePlacementContext();
        context.owner = this;
        contexts.add(context);
        RotationPlacementCheck check = new RotationPlacementCheck();
        check.context = context;
        checks.add(check); context.checks.add(check);
        context.checkMap.put(RotationPlacementCheck.class, check);
    }
    public void reset() {
        snapshot = new PlacementSnapshot();
        for (SimplePlacementContext context : contexts)
            for (PlacementCheck<?> check : context.checkMap.values())
                if (check instanceof RotationPlacementCheck) ((RotationPlacementCheck) check).reset();
    }
    public void onPacket(Packet<?> packet, boolean before) {
        if (before) {
            if (packet instanceof C08PacketPlayerBlockPlacement) facingTracker.record((C08PacketPlayerBlockPlacement) packet);
            return;
        }
        for (PlacementCheck<?> check : checks) {
            if (check instanceof RotationPlacementCheck) {
                if (packet instanceof C08PacketPlayerBlockPlacement)
                    ((RotationPlacementCheck) check).onPlacement(snapshot, (C08PacketPlayerBlockPlacement) packet);
                else if (packet instanceof C03PacketPlayer)
                    ((RotationPlacementCheck) check).onMovement(snapshot, (C03PacketPlayer) packet);
            }
        }
        if (packet instanceof C08PacketPlayerBlockPlacement) facingTracker.remove((C08PacketPlayerBlockPlacement) packet);
    }
}
