package com.adjustclient.module.impl.render.blockoverlay;

import net.minecraft.util.AxisAlignedBB;

final class BlockTarget extends OverlayTarget {
    public final BlockOverlaySettings settings;
    public final AxisAlignedBB bounds;
    public final int color;
    public final int outlineColor;
    BlockTarget(BlockOverlaySettings settings, AxisAlignedBB bounds, int color, int outlineColor) {
        this.settings = settings;
        this.bounds = bounds;
        this.color = color;
        this.outlineColor = outlineColor;
    }
}
