package com.adjustclient.module.impl.combat.builder;

import com.adjustclient.Adjust;
import com.adjustclient.event.EventHandler;
import com.adjustclient.event.Listener;
import com.adjustclient.event.IngameActionEvent;
import com.adjustclient.event.Render3DEvent;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.module.impl.player.inventory.item.type.BlockItemType;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.render.animation.Animation;
import com.adjustclient.render.animation.Easing;
import com.adjustclient.rotation.LinearRotationModel;
import com.adjustclient.rotation.handler.RotationHandler;
import com.adjustclient.util.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import com.adjustclient.util.PlacementFacingMode;

public final class BuilderPlacer implements Listener, MinecraftInstance {
    public static BuilderPlacer INSTANCE;
    public static boolean placementRotationActive;
    public BuildTarget target;
    public BlockPlacement placement;
    public final BuilderModule owner;
    public int interactTicks;
    public int waitTicks;
    public int slot = -1;

    public BuilderPlacer(BuilderModule owner) { this.owner = owner; INSTANCE = this; }

    @EventHandler(priority = 46)
    public void onPreUpdate(IngameActionEvent event) {
        if (!owner.isEnabled()) { placement = null; slot = -1; return; }
        placement = null;
        int previousWait = waitTicks;
        if (waitTicks >= 1) --waitTicks;
        if (target == null) { slot = -1; return; }
        if (target.ordered && slot >= 0) {
            Adjust.slotSpoofer.slot = slot;
            Adjust.slotSpoofer.active = true;
            Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.renderSpoofed = false;
        } else slot = -1;
        if (target.ordered) {
            while (!target.pending.isEmpty()) {
                BuilderBlockFilter first = target.pending.get(0);
                if (!first.matches(mc.theWorld.getBlockState(first.position))) {
                    if (!BlockUtil.getBlock(first.position).isReplaceable(mc.theWorld, first.position)) {
                        target = null; placement = null; slot = -1; waitTicks = 0;
                        return;
                    }
                    break;
                }
                first.completed = true;
                target.pending.remove(0);
            }
        } else {
            target.pending.removeIf(position -> {
                if (!mc.theWorld.getBlockState(position.position).getBlock().isReplaceable(mc.theWorld, position.position)) {
                    position.completed = true;
                    return true;
                }
                return false;
            });
        }
        target.updateAllDown();
        if (target.pending.isEmpty()) return;
        Integer hardestSlot = null;
        Integer softestSlot = null;
        if (!target.ordered) {
            double highest = Double.MIN_VALUE;
            Function<ItemStack, Double> harder = stack -> hardBlockScore(stack);
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack == null) continue;
                Double score = harder.apply(stack);
                if (score != null && score > highest) { highest = score; hardestSlot = i; }
            }
            double lowest = Double.MAX_VALUE;
            Function<ItemStack, Double> softer = stack -> softBlockScore(stack);
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack == null) continue;
                Double score = softer.apply(stack);
                if (score != null && score < lowest) { lowest = score; softestSlot = i; }
            }
            if (hardestSlot == null) return;
        }
        List<BuilderBlockFilter> candidates;
        if (target.ordered) candidates = List.of(target.pending.get(0));
        else {
            candidates = new ArrayList<>(target.pending);
            candidates.sort(Comparator.comparingDouble(position ->
                    -position.position.distanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)));
        }
        for (BuilderBlockFilter candidate : candidates) {
            if (candidate.facing == EnumFacing.UP && !target.allDown) continue;
            BlockPlacement hit = candidate.cachedPlacement != null ? candidate.cachedPlacement : findPlacement(candidate, 4.0F);
            if (hit == null) continue;
            if (candidate.type != null) {
                slot = selectMaterialSlot(candidate);
                if (slot < 0) return;
            } else slot = candidate.preferSoft && softestSlot != null ? softestSlot : hardestSlot;
            placement = hit;
            boolean interactable = isPlacementInteractable();
            Adjust.slotSpoofer.slot = slot;
            Adjust.slotSpoofer.active = true;
            Adjust.slotSpoofer.field8 = Adjust.slotSpoofer.field12;
            Adjust.slotSpoofer.field17 = false;
            Adjust.slotSpoofer.renderSpoofed = false;
            RotationHandler.INSTANCE.target = hit.angles;
            double rotationBase = switch (target.waitToPlace) {
                case 1 -> 30.0;
                case 2 -> 25.0;
                case 3 -> 20.0;
                default -> 60.0;
            };
            RotationHandler.INSTANCE.model = new LinearRotationModel(rotationBase
                    + RandomUtil.nextFloat(0.01F, 0.05F));
            RotationHandler.INSTANCE.requested = true;
            RotationHandler.INSTANCE.active = true;
            RotationHandler.release();
            if (interactable && interactTicks == 0) { interactTicks = 2; return; }
            if (previousWait >= 1 && target.waitToPlace >= 1) return;
            MovingObjectPosition traced = WorldUtil.pickFromPlayer(false, mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch, mc.playerController.getBlockReachDistance());
            if (traced == null || traced.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                    || !hit.pos.equals(traced.getBlockPos()) || hit.facing != traced.sideHit) return;
            if (mc.theWorld.checkNoEntityCollision(new AxisAlignedBB(hit.pos.offset(hit.facing)), null)) {
                WorldUtil.setMouseOver(traced);
                Adjust.mouseClickHelper.right.clicks = 1;
                Adjust.mouseClickHelper.right.down = true;
                Adjust.mouseClickHelper.right.holdTicks = 0;
                Adjust.cpsCounter.recordClick();
                waitTicks = target.waitToPlace;
            }
            placement = null;
            if (!target.ordered) slot = -1;
            return;
        }
        if (!target.ordered) { target = null; placement = null; slot = -1; waitTicks = 0; }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (!owner.isEnabled() || target == null || !target.render) return;
        RenderUtil.begin3d();
        double cameraX = mc.getRenderManager().viewerPosX;
        double cameraY = mc.getRenderManager().viewerPosY;
        double cameraZ = mc.getRenderManager().viewerPosZ;
        for (BuilderBlockFilter position : target.positions) {
            int rgb = -5032394;
            if (position.completed) {
                if (position.animation == null) position.animation = new Animation(Easing.LINEAR, 110);
                position.animation.update(1.0F);
                rgb = Color.HSBtoRGB(position.animation.value / 3.0F, 0.7F, 0.7F);
            }
            Color color = new Color(rgb);
            GlStateManager.color(color.getRed() / 255.0F, color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F, 60.0F / 255.0F);
            Supplier<AxisAlignedBB> fallback = () -> new AxisAlignedBB(position.position);
            AxisAlignedBB box = BlockUtil.getCollisionBox(position.position);
            if (box == null) box = fallback.get();
            box = box.offset(-cameraX, -cameraY, -cameraZ);
            AxisAlignedBB fill = box.expand(-0.0019D, -0.0019D, -0.0019D);
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            GL11.glVertex3d(fill.minX, fill.minY, fill.minZ);
            GL11.glVertex3d(fill.minX, fill.maxY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.minY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.maxY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.minY, fill.maxZ);
            GL11.glVertex3d(fill.maxX, fill.maxY, fill.maxZ);
            GL11.glVertex3d(fill.minX, fill.minY, fill.maxZ);
            GL11.glVertex3d(fill.minX, fill.maxY, fill.maxZ);
            GL11.glVertex3d(fill.minX, fill.minY, fill.minZ);
            GL11.glVertex3d(fill.minX, fill.maxY, fill.minZ);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(fill.minX, fill.minY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.minY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.minY, fill.maxZ);
            GL11.glVertex3d(fill.minX, fill.minY, fill.maxZ);
            GL11.glEnd();
            GlStateManager.cullFace(GL11.GL_FRONT);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex3d(fill.minX, fill.maxY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.maxY, fill.minZ);
            GL11.glVertex3d(fill.maxX, fill.maxY, fill.maxZ);
            GL11.glVertex3d(fill.minX, fill.maxY, fill.maxZ);
            GL11.glEnd();
            GlStateManager.cullFace(GL11.GL_BACK);
            RenderUtil.drawOutlinedBox(box);
        }
        RenderUtil.end3d();
    }

    public static Double softBlockScore(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) return null;
        ItemBlock item = (ItemBlock) stack.getItem();
        if (!BlockItemType.isValidBlock(item)) return null;
        Block block = item.getBlock();
        if (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() != 1.0D
                || block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() != 1.0D
                || block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() != 1.0D) return null;
        return block == Blocks.wool ? -1.0D : (double) block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN);
    }

    public BlockPlacement findPlacement(BuilderBlockFilter position, float accuracy) {
        BlockPos pos = position.position;
        if (!mc.theWorld.getBlockState(pos).getBlock().isReplaceable(mc.theWorld, pos)) return null;
        for (Entity entity : mc.theWorld.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!entity.isDead && entity.preventEntitySpawning) return null;
        }
        Vec3 origin = mc.thePlayer.getPositionVector();
        Vec3 closest = DistanceUtil.closestPoint(origin, new AxisAlignedBB(pos));
        double reach = mc.playerController.getBlockReachDistance();
        if (origin.squareDistanceTo(closest) >= reach * reach) return null;
        placementRotationActive = position.facing == EnumFacing.UP || target == null || target.rotate;
        try {
            return RotationUtil.findPlacement(mc.thePlayer, pos,
                    new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch),
                    PlacementFacingMode.DEFAULT, false, 0.95D, 0.95D, accuracy);
        } finally {
            placementRotationActive = false;
        }
    }

    public static Double hardBlockScore(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemBlock)) return null;
        ItemBlock item = (ItemBlock) stack.getItem();
        if (!BlockItemType.isValidBlock(item)) return null;
        Block block = item.getBlock();
        if (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() != 1.0D
                || block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() != 1.0D
                || block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() != 1.0D) return null;
        return (double) block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN);
    }

    public int selectMaterialSlot(BuilderBlockFilter position) {
        int selected = Adjust.slotSpoofer.resolveSlot(mc.thePlayer.inventory.currentItem);
        if (position.matches(mc.thePlayer.inventory.mainInventory[selected])) return selected;
        for (int i = 0; i < 9; ++i) {
            if (position.matches(mc.thePlayer.inventory.mainInventory[i])) return i;
        }
        return -1;
    }

    public boolean isPlacementInteractable() {
        if (placement == null) return false;
        return BlockUtil.isInteractable(mc.theWorld.getBlockState(placement.pos).getBlock());
    }

    public boolean isEnabled() {
        if (!owner.isEnabled()) return false;
        if (ScaffoldModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.placeReady) return false;
        return GameStateUtil.isActive();
    }
}
