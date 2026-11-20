package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.Block;
import com.adjustclient.utilities.script.api.Item;

public final class ItemStack {
    public ItemStack(Block blockIn) {
    }

    public ItemStack(Block blockIn, int amount) {
    }

    public ItemStack(Block blockIn, int amount, int meta) {
    }

    public ItemStack(Item itemIn) {
    }

    public ItemStack(Item itemIn, int amount) {
    }

    public ItemStack(Item itemIn, int amount, int meta) {
    }

    public native Item getItem();

    public native ItemStack copy();

    public native int getItemId();

    public native int getStackSize();

    public native int getMaxStackSize();

    public native String getDisplayName();

    public native int getItemDamage();

    public native int getMetadata();

    public native int getMaxDamage();

    public native boolean isItemDamaged();

    public native boolean isItemEnchanted();
}

