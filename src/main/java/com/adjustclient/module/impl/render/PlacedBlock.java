package com.adjustclient.module.impl.render;

import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public final class PlacedBlock {
    public AxisAlignedBB bounds;
    public Timer timer;
    public BlockPlacesModule module;
    public int blockId;

    public PlacedBlock(BlockPlacesModule module, BlockPos position, Block block) {
        this.timer = new Timer();
        this.bounds = block.getSelectedBoundingBox(MinecraftInstance.mc.theWorld, position)
                .expand(0.002000000094994903, 0.002000000094994903, 0.002000000094994903);
        this.module = module;
        this.blockId = Block.getIdFromBlock(block);
    }

    public int getFadeAlpha() {
        Number duration = (Number) ((com.adjustclient.setting.NumberSetting) module.timeout).getValue();
        int base = (int) (duration.doubleValue() * 1000.0);
        long elapsed = System.currentTimeMillis() - timer.lastMs;
        if (base > elapsed) return 255;
        long remaining = base + 500L - elapsed;
        if (remaining < 30L) return 0;
        return (int) ((float) remaining / 500.0F * 255.0F);
    }
}
