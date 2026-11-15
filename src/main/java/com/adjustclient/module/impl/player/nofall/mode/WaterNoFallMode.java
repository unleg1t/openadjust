package com.adjustclient.module.impl.player.nofall.mode;
import com.adjustclient.Adjust;
import com.adjustclient.event.*;
import com.adjustclient.module.impl.player.nofall.*;
import com.adjustclient.rotation.InstantRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.util.*;
import com.adjustclient.util.anticheat.ServerTracker;
import java.util.*;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Pair;
import com.adjustclient.util.PlacementFacingMode;
public final class WaterNoFallMode extends NoFallMode {
    public WaterPlacement placement;
    public WaterNoFallMode(NoFallModule module) { super(module, "Water"); }
    @EventHandler(priority = 110) public void onUseItem(AttackInputStageEvent event) {
        if (!isActive()) return;
        if (placement != null) {
            if (placement.placed) {
                if (selectBucket(Items.bucket)) {
                    Block block = mc.theWorld.getBlockState(placement.waterPos).getBlock();
                    if (block instanceof BlockLiquid && block.getMaterial() == Material.water) {
                        Adjust.mouseClickHelper.right.clicks = 1;
                        Adjust.mouseClickHelper.right.down = true;
                        Adjust.mouseClickHelper.right.holdTicks = 0;
                        WorldUtil.setMouseOver(placement.pickupHit);
                        event.setCancelled(true);
                    }
                }
                placement = null;
                return;
            }
            Pair<Vec2f, MovingObjectPosition> hit = findWaterRotation(placement.waterPos, mc.thePlayer);
            if (hit == null || !selectBucket(Items.water_bucket)) { placement = null; return; }
            Adjust.mouseClickHelper.right.clicks = 1;
            Adjust.mouseClickHelper.right.down = true;
            Adjust.mouseClickHelper.right.holdTicks = 0;
            mc.pointedEntity = null;
            mc.objectMouseOver = new MovingObjectPosition(placement.target.hitVec, placement.target.facing, placement.target.pos);
            ServerTracker.INSTANCE.mouseOverOverridden = true;
            RotationHandler.INSTANCE.target = hit.getLeft();
            RotationHandler.INSTANCE.model = InstantRotationModel.INSTANCE;
            RotationHandler.INSTANCE.requested = true;
            RotationHandler.INSTANCE.active = true;
            RotationHandler.release();
            placement.pickupHit = hit.getRight();
            placement.placed = true;
            event.setCancelled(true);
            return;
        }
        if (mc.thePlayer.onGround
                || mc.thePlayer.fallDistance < WorldUtil.safeFallDistance(mc.thePlayer) + mc.thePlayer.motionY
                || !module.canUse() || mc.thePlayer.isInWater()) { placement = null; return; }
        Simulation first = new Simulation(mc.thePlayer);
        first.player.tick();
        if (first.player.onGround) return;
        Simulation second = new Simulation(first.player);
        second.player.tick();
        if (!second.player.onGround) return;
        BlockPos waterPos = new BlockPos(mc.thePlayer.posX + mc.thePlayer.motionX,
                second.player.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ);
        float factor = RandomUtil.jitter(0.92000002F, 0.0099999998F);
        BlockPlacement target = RotationUtil.findPlacement(first.player, waterPos,
                new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), PlacementFacingMode.DEFAULT,
                true, factor, factor, 7.0F);
        if (target == null || !selectBucket(Items.water_bucket)) return;
        RotationHandler.INSTANCE.target = target.angles;
        RotationHandler.INSTANCE.model = InstantRotationModel.INSTANCE;
        RotationHandler.INSTANCE.requested = true;
        RotationHandler.INSTANCE.active = true;
        RotationHandler.release();
        placement = new WaterPlacement(target, waterPos);
    }
    public boolean selectBucket(Item item) {
        int start = Adjust.slotSpoofer.resolveSlot(mc.thePlayer.inventory.currentItem);
        Function<Integer, ItemStack> slotStack = slot -> mc.thePlayer.inventory.mainInventory[slot];
        Integer selected = WorldUtil.findHotbarSlot(start, slot -> {
            ItemStack stack = slotStack.apply(slot);
            return stack != null && stack.getItem() == item;
        });
        if (selected == null) return false;
        Adjust.slotSpoofer.slot = selected;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = true;
        Adjust.slotSpoofer.renderSpoofed = false;
        return true;
    }
    public Pair<Vec2f, MovingObjectPosition> findWaterRotation(BlockPos pos, Entity entity) {
        float factor = RandomUtil.jitter(0.91000003F, 0.0099999998F);
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        List<HitVec> candidates = RotationUtil.getSortedHitVecs(entity, new AxisAlignedBB(pos, pos.add(1, 1, 1)),
                EnumFacing.UP, factor, factor, 4.0F);
        Pair<HitVec, MovingObjectPosition> result = RotationUtil.findHitRotation(candidates, block, pos);
        return result == null ? null : Pair.of(result.getLeft().angles, result.getRight());
    }
    public static final class WaterPlacement {
        public final BlockPlacement target;
        public final BlockPos waterPos;
        public MovingObjectPosition pickupHit;
        public boolean placed;
        public WaterPlacement(BlockPlacement target, BlockPos waterPos) { this.target = target; this.waterPos = waterPos; }
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass() {
        com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                com.adjustclient.service.telemetry.BypassFeature.NOFALL_WATER);
    }
}
