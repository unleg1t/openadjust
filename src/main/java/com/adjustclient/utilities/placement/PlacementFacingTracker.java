package com.adjustclient.utilities.placement;
import com.adjustclient.utilities.MinecraftInstance;
import java.util.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
public final class PlacementFacingTracker implements MinecraftInstance {
    public PlacementVerifier owner;
    public Map<C08PacketPlayerBlockPlacement, BlockPlacementSample> pending = new HashMap<>();
    public Object field12;
    public PlacementFacingTracker(PlacementVerifier owner) { this.owner = owner; }
    public void record(C08PacketPlayerBlockPlacement packet) {
        if (packet.getPlacedBlockDirection() == 255) return;
        BlockPos pos = packet.getPosition();
        EnumFacing facing = EnumFacing.getFront(packet.getPlacedBlockDirection());
        Vec3 hit = new Vec3(packet.getPlacedBlockOffsetX(), packet.getPlacedBlockOffsetY(), packet.getPlacedBlockOffsetZ());
        BlockPos adjacent = pos.offset(facing);
        if (packet.getStack() == null || !(packet.getStack().getItem() instanceof ItemBlock)) return;
        if (((ItemBlock) packet.getStack().getItem()).canPlaceBlockOnSide(mc.theWorld, adjacent, facing, mc.thePlayer, packet.getStack())) {
            pending.put(packet, new BlockPlacementSample(owner.snapshot, pos, facing, hit, adjacent));
        }
    }
    public void remove(C08PacketPlayerBlockPlacement packet) {
        BlockPlacementSample sample = pending.remove(packet);
        if (sample == null) return;
        for (PlacementCheck<?> check : owner.checks) java.util.Objects.requireNonNull(check);
    }
    public boolean contains(C08PacketPlayerBlockPlacement packet) { return pending.containsKey(packet); }
}
