package com.adjustclient.module.impl.movement.scaffold;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

public record FaceBox(AxisAlignedBB boundingBox, EnumFacing facing) {
}
