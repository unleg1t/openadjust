package com.adjustclient.modules.impl.player.autohypixel.feature.bedwars.tracker.item;

import com.adjustclient.utilities.Timer;
import net.minecraft.item.ItemStack;

public final class TrackedItem {
    public final ItemStack stack;
    public final Timer timer;
    public boolean superseded;
    public TrackedItem(ItemStack stack) { this.stack = stack; this.timer = new Timer(); }
}
