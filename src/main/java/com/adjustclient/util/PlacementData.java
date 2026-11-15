package com.adjustclient.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

public final class PlacementData {
    public Object field4;
    public Object field8;
    public Object field12;
    public Object field16;
    public Object field20;
    public Object field24;
    public Object field28;
    public Object field32;

    public PlacementData() {}

    public PlacementData(BlockPos pos, EnumFacing facing, Vec3 hit, Object item, EntityPlayer player) {
        this.field4 = new FrozenTimer();
        this.field8 = pos;
        this.field12 = facing;
        this.field16 = hit;
        this.field20 = item;
        this.field24 = new Vec3(player.posX == -0.0 ? 0.0 : player.posX,
                player.posY == -0.0 ? 0.0 : player.posY,
                player.posZ == -0.0 ? 0.0 : player.posZ);
        this.field28 = new Vec2f(player.rotationYaw, player.rotationPitch);
    }
}
