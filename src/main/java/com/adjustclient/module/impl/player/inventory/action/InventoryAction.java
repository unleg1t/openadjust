package com.adjustclient.module.impl.player.inventory.action;
import com.adjustclient.module.impl.player.inventory.click.InventoryClicker;
import com.adjustclient.util.MinecraftInstance;
public abstract class InventoryAction implements MinecraftInstance {
    public abstract void execute(InventoryClicker clicker);
}
