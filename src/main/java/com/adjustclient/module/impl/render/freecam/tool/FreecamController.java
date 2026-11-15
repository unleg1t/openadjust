package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.Adjust;
import com.adjustclient.event.CancellableEvent;
import com.adjustclient.game.packet.PacketHoldHandle;
import com.adjustclient.hypixel.game.HousingGameState;
import com.adjustclient.module.impl.render.freecam.FreecamControlMode;
import com.adjustclient.module.impl.render.freecam.FreecamModule;
import com.adjustclient.module.impl.render.hud.HUDModule;
import com.adjustclient.render.font.image.NvgDraw;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.util.BlockUtil;
import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.RandomUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.Vec2f;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class FreecamController implements MinecraftInstance {
    public final FreecamModule module;
    public final PathfinderThread worker;
    public final Object lock = new Object();
    public final Timer timer = new Timer();
    public Object field20;
    public Object field24;
    public final PacketHoldHandle hold;
    public final ConcurrentHashMap<BlockPos, Integer> blocks = new ConcurrentHashMap<>();
    public Object field36;

    public FreecamController(FreecamModule module) {
        this.module = module;
        this.hold = new PacketHoldHandle(Adjust.inboundPacketQueue);
        this.worker = new PathfinderThread(this);
        this.worker.start();
    }

    public void stop() {
        hold.release();
        worker.interrupt();
    }

    @com.adjustclient.event.EventHandler
    public void allowRecentUpdate(com.adjustclient.event.NetworkFlushEvent event) {
        if (!timer.hasElapsed(5L)) event.flushAllowed = false;
    }

    public void updateBalance(CancellableEvent event) {
        timer.hasElapsed(0L);
        if (Adjust.gameStateTracker.state instanceof HousingGameState housing && housing.field52 >= 1) {
            hold.acquire(hold.field16);
            --housing.field52;
            if (module.packetBalanceRegain.isEnabled()) event.setCancelled(true);
        } else {
            hold.release();
        }
        net.minecraft.entity.Entity viewEntity = mc.cameraOverride != null ? mc.cameraOverride : mc.thePlayer;
        for (Map.Entry<BlockPos, Integer> entry : blocks.entrySet()) {
            int encoded = entry.getValue();
            Block block = Block.getBlockById(encoded & 0xFFF);
            String sound = block.stepSound.getBreakSound();
            mc.getSoundHandler().playSound(new net.minecraft.client.audio.PositionedSoundRecord(
                    new net.minecraft.util.ResourceLocation(sound), block.stepSound.getVolume(),
                    block.stepSound.getFrequency() * 0.8F,
                    (float) viewEntity.posX, (float) viewEntity.posY, (float) viewEntity.posZ));
            mc.effectRenderer.addBlockDestroyEffects(entry.getKey(), block.getStateFromMeta((encoded >> 12) & 255));
        }
        blocks.clear();
    }

    public void renderBalance(com.adjustclient.event.Render2DEvent event) {
        float balance = Adjust.packetBalance.balance;
        renderProgress(event, balance > 19.0F ? 1.0F : balance / 19.0F, 0.0F);
    }

    public void recordBlock(BlockPos position) {
        IBlockState state = mc.theWorld.getBlockState(position);
        Block block = state.getBlock();
        blocks.put(position, Block.getIdFromBlock(block) + (block.getMetaFromState(state) << 12));
    }

    public void queueAction(FreecamControlMode mode, int button) {
        if (worker.pending != null && !worker.pending.complete) return;
        MovingObjectPosition hit = (MovingObjectPosition) module.hitPosition;
        if (hit == null || !mc.playerController.isInCreativeMode()) return;
        if (Adjust.gameStateTracker.state instanceof HousingGameState housing && housing.field52 >= 1) return;
        FreecamTool tool;
        if (mode == FreecamControlMode.DECIMATOR && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && button <= 1) {
            tool = new DecimatorTool(this, hit, button == 0
                    ? com.adjustclient.module.impl.render.freecam.FreecamClickType.NUKE
                    : com.adjustclient.module.impl.render.freecam.FreecamClickType.EXTEND);
        } else if (mode == FreecamControlMode.TELEPORT && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            tool = new EmptyTool(this, hit);
        } else if (mode == null && button == 0 && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            tool = new InteractTool(this, hit);
        } else if (mode == null && button == 1) {
            ItemStack selected = module.camera.distanceToCamera();
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && hit.getBlockPos().distanceSq(new BlockPos(new Vec3(module.camera.posX, module.camera.posY, module.camera.posZ))) < 49.0D
                    && (selected == null || selected.getItem().getClass() != ItemPotion.class)) {
                Block target = mc.theWorld.getBlockState(hit.getBlockPos()).getBlock();
                tool = BlockUtil.isInteractable(target) && !(target instanceof net.minecraft.block.BlockSign)
                        ? new PlayerControlTool(this, hit) : new BlockPlaceTool(this, hit);
            } else {
                if (selected == null || selected.getItem() instanceof net.minecraft.item.ItemBlock) return;
                tool = new CreativePlaceTool(this, hit, new Vec3(module.camera.posX, module.camera.posY, module.camera.posZ),
                        new Vec2f(module.camera.rotationYaw, module.camera.rotationPitch), selected.getItem() instanceof ItemPotion);
            }
        } else return;
        BlockPos destination = tool instanceof CreativePlaceTool
                ? new BlockPos(module.camera.posX, module.camera.posY, module.camera.posZ) : hit.getBlockPos();
        PendingAction action = new PendingAction(tool, new Vec3(destination),
                new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        worker.pending = action;
        synchronized (lock) { lock.notifyAll(); }
    }

    @SuppressWarnings("unused")
    private static void vendorReportBypass(FreecamTool tool) {
        com.adjustclient.service.util.ServiceUtil.reportFeatureUse(
                tool instanceof DecimatorTool
                        ? com.adjustclient.service.telemetry.BypassFeature.FREECAM_NUKER
                        : com.adjustclient.service.telemetry.BypassFeature.FREECAM_INVISIBLE);
    }

    public void renderProgress(com.adjustclient.event.Render2DEvent event, float progress, float offset) {
        ScaledResolution resolution = event.scaledResolution;
        float x = resolution.getScaledWidth() / 2.0F - 89.0F;
        float y = resolution.getScaledHeight() - 3.0F - 28.0F - offset;
        NvgDraw.fillGradientRect(0xFF232323, 0xFF191919, x, y, x + 178.0F, y + 3.0F);
        Color color = HUDModule.animatedColor != null ? HUDModule.animatedColor : HUDModule.colorSetting.getColor();
        if (progress > 0.0F) {
            Color dark = com.adjustclient.render.ColorUtil.scale(color, 0.5F);
            NvgDraw.fillGradientRect(color.getRGB(), dark.getRGB(), x, y,
                    x + Math.min(progress, 1.0F) * 178.0F, y + 3.0F);
        }
        NvgDraw.strokeRect(true, 0xFF0F0F0F, x, y, x + 178.0F, y + 3.0F, 1.0F);
    }

    public void execute(PendingAction action) {
        worker.pending = null;
        if (action.path == null || action.path.isEmpty()) {
            NotificationBuilder notification = new NotificationBuilder();
            notification.title = "Freecam";
            notification.parts = new ArrayList<>();
            notification.actions = new ArrayList<>();
            notification.addText("Pathfinding failed. The blocks you are trying to edit may not be accessible.", 0xFFBEBCBD);
            notification.lifetimeSteps = 38;
            notification.show();
            return;
        }
        ArrayList<Vec3> path = new ArrayList<>(action.path);
        ArrayList<Packet<?>> packets = new ArrayList<>();
        int originalSlot = mc.thePlayer.inventory.m();
        boolean switchSlot = action.tool.field12 && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
        if (switchSlot) {
            int selected = -1;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
                if (stack == null || !(stack.getItem() instanceof ItemSword)) { selected = slot; break; }
            }
            if (selected < 0) {
                NotificationBuilder notification = new NotificationBuilder();
                notification.title = "Freecam";
                notification.parts = new ArrayList<>();
                notification.actions = new ArrayList<>();
                notification.addText("Your hotbar must contain at least one slot without a sword.", 0xFFBEBCBD);
                return;
            }
            packets.add(new C09PacketHeldItemChange(selected));
        }
        appendPath(packets, path);
        int before = packets.size();
        action.tool.execute(packets, path.get(path.size() - 1));
        if (!(action.tool instanceof EmptyTool)) {
            if (before == packets.size()) return;
            Collections.reverse(path);
            Vec3 restore = path.isEmpty() ? null : path.remove(path.size() - 1);
            appendPath(packets, path);
            if (restore != null) packets.add(new TeleportPath().packet(restore));
        } else {
            Vec3 destination = path.get(path.size() - 1);
            mc.thePlayer.setPosition(destination.xCoord, destination.yCoord, destination.zCoord);
        }
        if (switchSlot) packets.add(new C09PacketHeldItemChange(originalSlot));
        field24 = null;
        for (Packet<?> packet : packets) {
            mc.thePlayer.sendQueue.getNetworkManager().sendPacket(packet);
            if (packet instanceof C03PacketPlayer && Adjust.gameStateTracker.state instanceof HousingGameState housing) {
                housing.field56 = ++housing.field52;
            }
        }
    }

    public void appendPath(List<Packet<?>> packets, List<Vec3> path) {
        for (Vec3 point : path) {
            double y = point.yCoord + RandomUtil.nextDouble(0.001D, 0.0018D);
            packets.add(new C03PacketPlayer.C04PacketPlayerPosition(point.xCoord, y, point.zCoord,
                    y % 0.015625D == 0.0D));
        }
    }
}
