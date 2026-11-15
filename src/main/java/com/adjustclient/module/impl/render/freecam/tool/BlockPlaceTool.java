package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.util.BlockPlacement;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.HitVec;
import com.adjustclient.util.PlacementFacingMode;
import com.adjustclient.util.RotationUtil;
import com.adjustclient.util.Vec2f;
import com.adjustclient.util.WorldUtil;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import com.adjustclient.module.impl.combat.builder.BuilderPlacer;

public final class BlockPlaceTool extends FreecamTool {
    public Object field16;

    public BlockPlaceTool(FreecamController controller, MovingObjectPosition hit) { super(controller, hit); }

    @Override public void execute(List<Packet<?>> packets, Vec3 position) {
        Vec2f rotation = ServerTracker.INSTANCE.rotation;
        if (rotation == null) return;
        BlockPos target = hit.getBlockPos().offset(hit.sideHit);
        double oldX = mc.thePlayer.posX;
        double oldY = mc.thePlayer.posY;
        double oldZ = mc.thePlayer.posZ;
        mc.thePlayer.setPosition(position.xCoord, position.yCoord, position.zCoord);
        BlockPlacement placement = null;
        MovingObjectPosition initial = WorldUtil.rayTrace(mc.thePlayer,
                mc.playerController.getBlockReachDistance(), rotation.x, rotation.y);
        if (initial != null && initial.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && RotationUtil.isPlacementTarget(initial, target)
                && !RotationUtil.isInteractable(initial.getBlockPos())) {
            placement = new BlockPlacement(initial.getBlockPos(), initial.sideHit, initial.hitVec, rotation);
        } else {
            List<HitVec> candidates = new ArrayList<>();
            for (EnumFacing face : EnumFacing.values()) {
                IBlockState state = mc.theWorld.getBlockState(target);
                Block block = state.getBlock();
                if (!BuilderPlacer.placementRotationActive && (BlockUtil.INTERACTABLE_BLOCKS.contains(block)
                        || block instanceof BlockDoor || block instanceof BlockFence || block instanceof BlockFenceGate)
                        && (!mc.thePlayer.movementInput.sneak || mc.thePlayer.isPlayerSleeping())) continue;
                BlockPos neighbor = target.offset(face);
                AxisAlignedBB box = BlockUtil.getCollisionBox(neighbor);
                if (box == null) continue;
                List<HitVec> sampled = RotationUtil.getHitVecs(mc.thePlayer, box, face, 1.0D, 1.0D, 7.0F);
                for (HitVec candidate : sampled) candidate.blockPos = neighbor;
                candidates.addAll(sampled);
            }
            candidates.sort(Comparator.comparingDouble(candidate -> {
                float yaw = MathHelper.wrapAngleTo180_float(rotation.x - candidate.angles.x);
                float pitch = rotation.y - candidate.angles.y;
                double score = yaw * yaw + pitch * pitch;
                for (double coordinate : new double[] {candidate.xCoord, candidate.yCoord, candidate.zCoord}) {
                    double value = Math.abs(coordinate);
                    if (value != 0.0D && value != 1.0D)
                        score *= 1.0D + (1.0D - Math.abs(0.5D - value) * 2.0D) * 0.05D;
                }
                return score;
            }));
            Pair<HitVec, MovingObjectPosition> selected = RotationUtil.findFilteredHitRotation(candidates, mc.thePlayer, false,
                    ray -> RotationUtil.isAllowedPlacementHit(PlacementFacingMode.DEFAULT, target, ray), true);
            if (selected != null) {
                MovingObjectPosition ray = selected.getRight();
                placement = new BlockPlacement(ray.getBlockPos(), ray.sideHit,
                        Objects.requireNonNull(ray.hitVec, "hitVec"), selected.getLeft().angles);
            }
        }
        mc.thePlayer.setPosition(oldX, oldY, oldZ);
        if (placement == null) return;
        ItemStack previous = mc.thePlayer.inventory.getCurrentItem();
        ItemStack[] inventory;
        if (controller.module.camera.state == com.adjustclient.module.impl.render.freecam.FreecamState.HOUSING) {
            inventory = controller.module.field72.mainInventory;
        } else if (controller.module.camera.state == com.adjustclient.module.impl.render.freecam.FreecamState.CHANGE_SPEED) {
            inventory = controller.module.camera.speedItems;
        } else {
            inventory = controller.module.camera.hotbar;
        }
        int selectedSlot = controller.module.camera.selectedSlot();
        ItemStack selected = selectedSlot >= 0 && selectedSlot < 9 ? inventory[selectedSlot] : null;
        if (selected == null) return;
        boolean replace = previous != selected;
        int slot = mc.thePlayer.inventory.m() + 36;
        if (replace) packets.add(new C10PacketCreativeInventoryAction(slot, selected));
        Vec2f angles = placement.angles;
        packets.add(new C03PacketPlayer.C05PacketPlayerLook(
                angles.x - ((float) Math.random() * 0.01F + 1.3F) * 6.8999996F, angles.y, false));
        packets.add(new C03PacketPlayer.C05PacketPlayerLook(
                angles.x - ((float) Math.random() * 0.01F + 1.15F) * 3.8999999F, angles.y, false));
        packets.add(new C03PacketPlayer.C05PacketPlayerLook(
                angles.x - ((float) Math.random() * 0.01F + 1.0F) * 0.90000004F, angles.y, false));
        packets.add(new C03PacketPlayer.C05PacketPlayerLook(angles.x, angles.y, false));
        Vec3 relative = placement.hitVec.subtract(new Vec3(placement.pos));
        packets.add(new C0APacketAnimation());
        packets.add(new C08PacketPlayerBlockPlacement(placement.pos, placement.facing.getIndex(), selected,
                (float) relative.xCoord, (float) relative.yCoord, (float) relative.zCoord));
        int damage = selected.getItemDamage();
        int count = selected.stackSize;
        selected.onItemUse(mc.thePlayer, mc.theWorld, placement.pos, placement.facing,
                placement.pos.getX(), placement.pos.getY(), placement.pos.getZ());
        selected.setItemDamage(damage);
        selected.stackSize = count;
        if (replace) packets.add(new C10PacketCreativeInventoryAction(slot, previous));
        controller.module.camera.swingItem();
    }
}
