package com.adjustclient.core.events;

public class ItemUseNotificationEvent extends Event {
    public net.minecraft.item.ItemStack stack;
    public net.minecraft.util.BlockPos position;
    public net.minecraft.util.EnumFacing facing;

    public ItemUseNotificationEvent(net.minecraft.item.ItemStack stack, net.minecraft.util.BlockPos position,
            net.minecraft.util.EnumFacing facing) {
        this.stack = stack;
        this.position = position;
        this.facing = facing;
    }
}
