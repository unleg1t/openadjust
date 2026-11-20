package com.adjustclient.modules.impl.render.blockoverlay;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

final class TileEntityTarget extends OverlayTarget {
    public final RangedOverlaySettings settings;
    public final AxisAlignedBB bounds;
    public final TileEntity tileEntity;
    TileEntityTarget(RangedOverlaySettings settings, AxisAlignedBB bounds, TileEntity tileEntity) {
        this.settings = settings;
        this.bounds = bounds;
        this.tileEntity = tileEntity;
    }
}
