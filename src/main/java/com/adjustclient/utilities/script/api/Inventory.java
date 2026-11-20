package com.adjustclient.utilities.script.api;

import com.adjustclient.utilities.script.api.ItemStack;

public final class Inventory {
    public final int currentItem;

    public Inventory() {
        this.currentItem = 0;
    }

    public native void click(int var1, int var2, int var3);

    public native ItemStack getStackInSlot(int var1);

    public native ItemStack getCurrentItemStack();

    public native int getSizeInventory();

    private native int $script$read$currentItem();
}

