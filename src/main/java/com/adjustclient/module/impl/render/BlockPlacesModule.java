package com.adjustclient.module.impl.render;

import com.adjustclient.event.EventHandler;
import com.adjustclient.module.Category;
import com.adjustclient.module.Module;
import com.adjustclient.render.RenderUtil;
import com.adjustclient.setting.ColorSetting;
import com.adjustclient.setting.NumberSetting;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

public final class BlockPlacesModule extends Module {
    public static BlockPlacesModule INSTANCE;
    public final ColorSetting color;
    public final NumberSetting timeout;
    public final NumberSetting width;
    public final Object lock = new Object();
    public Map<BlockPos, PlacedBlock> placedBlocks;

    public BlockPlacesModule() {
        super("Block Places", Category.RENDER);
        INSTANCE = this;
        setHidden(false);
        color = new ColorSetting(this, "Color", new Color(0xff9090ff, true), true, false, ColorSetting.Mode.MAIN, null);
        timeout = new NumberSetting(this, "Timeout", 1.5, 0.5, 5.0, 0.5);
        width = new NumberSetting(this, "Width", 0.5, 0.5, 2.0, 0.5);
    }

    @EventHandler
    public void onUpdate(com.adjustclient.event.CancellablePlayerUpdateEvent event) {
        if (event.isCancelled() || !isEnabled() || placedBlocks == null) return;
        updateBlocks();
    }

    public void updateBlocks() {
        synchronized (lock) {
            placedBlocks.entrySet().removeIf(entry -> isExpired(entry));
        }
    }

    @com.adjustclient.event.EventHandler
    public void onBlockPlaced(com.adjustclient.event.BlockPlacedEvent event) {
        if (!isEnabled() || placedBlocks == null) return;
        synchronized (lock) {
            BlockPos position = event.pos;
            placedBlocks.put(position, new PlacedBlock(this, position, event.block));
        }
    }

    @EventHandler
    public void onRender3D(com.adjustclient.event.Render3DEvent event) {
        if (!isEnabled() || placedBlocks == null) return;
        renderBlocks();
    }

    public void renderBlocks() {
        if (placedBlocks.isEmpty()) return;
        RenderUtil.begin3d();
        GL11.glLineWidth(width.getFloat());
        GlStateManager.enableDepth();
        var manager = mc.getRenderManager();
        synchronized (lock) {
            for (PlacedBlock placed : placedBlocks.values()) {
                AxisAlignedBB bounds = placed.bounds.offset(-manager.viewerPosX, -manager.viewerPosY, -manager.viewerPosZ);
                RenderUtil.glColor((placed.getFadeAlpha() << 24) | (color.getColor().getRGB() & 0xffffff));
                RenderUtil.drawOutlinedBox(bounds);
            }
        }
        RenderUtil.end3d();
    }

    public boolean isExpired(Map.Entry<BlockPos, PlacedBlock> entry) {
        PlacedBlock placed = entry.getValue();
        if (placed.timer.lastMs + (int) (timeout.getFloat() * 1000.0f) + 470L <= System.currentTimeMillis()) return true;
        BlockPos position = entry.getKey();
        Block block = mc.theWorld.getBlockState(position).getBlock();
        if (Block.getIdFromBlock(block) != placed.blockId) return true;
        AxisAlignedBB bounds = block.getSelectedBoundingBox(mc.theWorld, position).expand(0.002000000094994903, 0.002000000094994903, 0.002000000094994903);
        return placed.bounds == null || !placed.bounds.equals(bounds);
    }

    @Override public void onEnable() { placedBlocks = new HashMap<>(); }
    @Override public void onDisable() { placedBlocks = null; }
    @Override public String getDescription() { return "Highlights blocks that you recently placed."; }
}
