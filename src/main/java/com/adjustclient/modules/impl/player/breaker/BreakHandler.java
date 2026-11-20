package com.adjustclient.modules.impl.player.breaker;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Pair;
import com.adjustclient.utilities.game.player.InventoryStateTracker;
import com.adjustclient.core.rotation.handler.RotationHandler;
import com.adjustclient.core.rotation.InstantRotationModel;

public class BreakHandler implements MinecraftInstance {
    public final BreakerModule module;
    public BlockReachCheck target;
    public Vec2f rotation;
    public float samplingFactor;
    public BreakHandler(BreakerModule module) { this.module = module; }

    public boolean tick() {
        if (target == null) return false;
        BlockPos pos = target.pos;
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        AxisAlignedBB box = block.getSelectedBoundingBox(mc.theWorld, pos);
        if (box == null || InventoryStateTracker.isInServersideGui() || !target.isValid()) {
            target = null; samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F); return false;
        }
        float eyeHeight = mc.thePlayer.isPlayerSleeping() ? 0.2F : 1.62F;
        if (!mc.thePlayer.isPlayerSleeping() && mc.thePlayer.movementInput.sneak) eyeHeight -= 0.08F;
        Vec3 eye = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + eyeHeight, mc.thePlayer.posZ);
        AxisAlignedBB bounds = target.box;
        double x = Math.max(bounds.minX, Math.min(bounds.maxX, eye.xCoord));
        double y = Math.max(bounds.minY, Math.min(bounds.maxY, eye.yCoord));
        double z = Math.max(bounds.minZ, Math.min(bounds.maxZ, eye.zCoord));
        double reach = getReach();
        if (eye.squareDistanceTo(new Vec3(x, y, z)) >= reach * reach) {
            target = null; samplingFactor = RandomUtil.nextFloat(0.05F, 0.06F); return false;
        }
        Pair<HitVec, MovingObjectPosition> hit = RotationUtil.findHitRotation(
                RotationUtil.getSortedHitVecs(mc.thePlayer, box, EnumFacing.UP,
                        samplingFactor, samplingFactor, 3.0F), target.block, pos);
        if (hit == null) return false;
        mc.rightClickDelayTimer = 0;
        if (module.hideSwings.isEnabled()) Adjust.mouseClickHelper.left.swing = false;
        apply(hit.getLeft().angles, hit.getRight(), module.preventInputEnabled.isEnabled(),
                module.rotations.isEnabled(), module.heldItem.isEnabled());
        return true;
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass(MovingObjectPosition hit) {
        boolean exposed = BreakerTargetFinder.isFaceExposed(hit.getBlockPos(), hit.sideHit);
        com.adjustclient.managers.util.ServiceUtil.reportFeatureUse(
                exposed
                        ? com.adjustclient.managers.telemetry.BypassFeature.BREAKER_BREAK
                        : com.adjustclient.managers.telemetry.BypassFeature.BREAKER_BREAK_AGAINST_BLOCK);
    }

    public void selectTool(boolean force) {
        if (target == null) return;
        ToolChoice tool = target.tool;
        if (tool == null || tool.slot == mc.thePlayer.inventory.m() && !force) return;
        Adjust.slotSpoofer.slot = tool.slot;
        Adjust.slotSpoofer.active = true;
        Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
        Adjust.slotSpoofer.field17 = false;
        Adjust.slotSpoofer.renderSpoofed = false;
    }

    public void setMouseOver(MovingObjectPosition hit) {
        Adjust.mouseClickHelper.right.disabled = true;
        Adjust.mouseClickHelper.left.disabled = true;
        WorldUtil.setMouseOver(hit);
        com.adjustclient.utilities.anticheat.ServerTracker.INSTANCE.mouseOverOverridden = true;
    }

    public boolean willFinish(BlockPos pos, boolean simulate) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        EntityPlayer player = mc.thePlayer;
        if (simulate) {
            Simulation simulation = new Simulation(mc.thePlayer);
            simulation.includeSpeedModule();
            SimulationPlayer simulated = simulation.player;
            player = simulated;
            simulated.tick();
        }
        ItemStack stack = getTool();
        float relative = BlockBreakUtil.getRelativeHardness(player, block, pos, stack, player.onGround);
        return mc.playerController.blockBreakingTicks * relative >= 1.0F;
    }

    public void apply(Vec2f angles, MovingObjectPosition hit, boolean preventInput, boolean spoofRotations, boolean spoofItem) {
        if (mc.playerController.isHittingBlock) {
            if (willFinish(hit.getBlockPos(), false)) {
                selectTool(false);
                if (!preventInput) setMouseOver(hit);
            }
            if (!preventInput) mc.sendClickBlockToControllerAt(mc.leftClickCounter == 0, hit);
            if (target != null && willFinish(hit.getBlockPos(), true)) rotation = angles;
        } else if (preventInput) {
            rotation = angles;
        } else {
            mc.thePlayer.sendQueue.netManager.protocolAdapter.interactionHandler.swingAndRun(
                    mc.thePlayer, com.adjustclient.utilities.protocol.type.SwingReason.DIGGING,
                    () -> {
                        if (mc.theWorld.getBlockState(hit.getBlockPos()).getBlock().getMaterial() != Material.air)
                            mc.playerController.clickBlock(hit.getBlockPos(), hit.sideHit);
                    });
            setMouseOver(hit);
            rotation = angles;
        }
        if (!spoofRotations) rotation = angles;
        if (!spoofItem) selectTool(true);
        if (!preventInput) return;
        var attack = Adjust.mouseClickHelper.left;
        if (!attack.previousDown) attack.clicks = 1;
        attack.down = Boolean.TRUE;
        attack.holdTicks = 2;
        Adjust.mouseClickHelper.right.disabled = true;
        WorldUtil.setMouseOver(hit);
    }

    public void updateRotation() {
        if (rotation != null) {
            RotationHandler.rotate(InstantRotationModel.INSTANCE, rotation.x, rotation.y);
            rotation = null;
        }
    }

    public ItemStack getTool() {
        if (target != null && target.tool != null) return target.tool.stack;
        return mc.thePlayer.inventory.getCurrentItem();
    }

    public static double getReach() {
        float base = mc.playerController.getBlockReachDistance();
        if (mc.playerController.curBlockDamageMP == 0.0F) return base;
        return (double) (base * 1.4F);
    }
}
