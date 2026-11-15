package com.adjustclient.script.api;

import com.adjustclient.script.api.ItemStack;
import com.adjustclient.script.api.Packet;

public final class C0EPacketClickWindow
extends Packet {
    public int windowId;
    public int slotId;
    public int usedButton;
    public short actionNumber;
    public ItemStack clickedItem;
    public int mode;

    public C0EPacketClickWindow() {
    }

    public C0EPacketClickWindow(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber) {
    }

    private native int $script$read$windowId();

    private native void $script$write$windowId(int var1);

    private native int $script$read$slotId();

    private native void $script$write$slotId(int var1);

    private native int $script$read$usedButton();

    private native void $script$write$usedButton(int var1);

    private native short $script$read$actionNumber();

    private native void $script$write$actionNumber(short var1);

    private native ItemStack $script$read$clickedItem();

    private native void $script$write$clickedItem(ItemStack var1);

    private native int $script$read$mode();

    private native void $script$write$mode(int var1);
}

